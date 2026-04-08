package com.backend.cuutro.services.impl;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.backend.cuutro.constant.enums.FileType;
import com.backend.cuutro.dto.request.TepTinUploadRequest;
import com.backend.cuutro.dto.response.entities.TepTinDto;
import com.backend.cuutro.entities.TepTinEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.TepTinMapper;
import com.backend.cuutro.repository.TepTinRepository;
import com.backend.cuutro.services.FileUploadService;
import com.backend.cuutro.services.TepTinService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TepTinServiceImpl implements TepTinService {

	private static final long IMAGE_MAX_BYTES = 8L * 1024 * 1024;
	private static final long VIDEO_MAX_BYTES = 80L * 1024 * 1024;
	private static final long AUDIO_MAX_BYTES = 80L * 1024 * 1024;

	private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg");
	private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv", "webm", "m4v");
	private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "ogg", "m4a", "aac", "flac");

	private static final Map<String, String> MIME_DEFAULT_EXTENSION = Map.ofEntries(
			Map.entry("image/jpeg", "jpg"),
			Map.entry("image/png", "png"),
			Map.entry("image/gif", "gif"),
			Map.entry("image/webp", "webp"),
			Map.entry("video/mp4", "mp4"),
			Map.entry("video/quicktime", "mov"),
			Map.entry("video/webm", "webm"),
			Map.entry("audio/mpeg", "mp3"),
			Map.entry("audio/wav", "wav"),
			Map.entry("audio/ogg", "ogg"),
			Map.entry("audio/mp4", "m4a"));

	private final FileUploadService fileUploadService;
	private final TepTinRepository tepTinRepository;
	private final TepTinMapper tepTinMapper;

	@Override
	@Transactional
	public TepTinDto upload(TepTinUploadRequest request) {
		MultipartFile tepTin = request.getTepTin();
		validateFileRequired(tepTin);

		FileType fileType = resolveFileType(tepTin);
		validateFileSize(tepTin, fileType);

		String thuMuc = normalizeSegment(request.getThuMuc());
		String extension = resolveExtension(tepTin);
		String tenTepGoc = resolveBaseFileName(request.getTenTep(), tepTin.getOriginalFilename());
		String tenTep = normalizeSegment(tenTepGoc);
		String folder = fileType.name().toLowerCase(Locale.ROOT) + "/" + thuMuc;
		String cloudinaryResourceType = toCloudinaryResourceType(fileType);

		String duongDan = fileUploadService.uploadFile(
				tepTin,
				folder,
				tenTep,
				cloudinaryResourceType,
				extension);

		TepTinEntity entity = new TepTinEntity();
		entity.setDuongDan(duongDan);
		entity.setLoaiTepTin(resolveLoaiTepTin(tepTin, fileType, extension));
		return tepTinMapper.toDto(tepTinRepository.save(entity));
	}

	private void validateFileRequired(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new InvalidFieldException("tepTin is required");
		}
	}

	private FileType resolveFileType(MultipartFile file) {
		String contentType = file.getContentType();
		if (StringUtils.hasText(contentType)) {
			String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
			if (normalizedContentType.startsWith("image/")) {
				return FileType.IMAGE;
			}
			if (normalizedContentType.startsWith("video/")) {
				return FileType.VIDEO;
			}
			if (normalizedContentType.startsWith("audio/")) {
				return FileType.AUDIO;
			}
		}

		String extension = resolveExtension(file);
		if (IMAGE_EXTENSIONS.contains(extension)) {
			return FileType.IMAGE;
		}
		if (VIDEO_EXTENSIONS.contains(extension)) {
			return FileType.VIDEO;
		}
		if (AUDIO_EXTENSIONS.contains(extension)) {
			return FileType.AUDIO;
		}
		throw new InvalidFieldException("tepTin chi ho tro dinh dang image/video/audio");
	}

	private void validateFileSize(MultipartFile file, FileType fileType) {
		long size = file.getSize();
		long maxAllowedSize = switch (fileType) {
			case IMAGE -> IMAGE_MAX_BYTES;
			case VIDEO -> VIDEO_MAX_BYTES;
			case AUDIO -> AUDIO_MAX_BYTES;
			default -> 0L;
		};
		if (size > maxAllowedSize) {
			throw new InvalidFieldException("Kich thuoc tep vuot qua gioi han cho phep");
		}
	}

	private String toCloudinaryResourceType(FileType fileType) {
		return switch (fileType) {
			case IMAGE -> "image";
			case VIDEO, AUDIO -> "video";
			default -> "auto";
		};
	}

	private String resolveExtension(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
			return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		}

		String contentType = file.getContentType();
		if (StringUtils.hasText(contentType)) {
			String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
			if (MIME_DEFAULT_EXTENSION.containsKey(normalizedContentType)) {
				return MIME_DEFAULT_EXTENSION.get(normalizedContentType);
			}
		}
		return "bin";
	}

	private String resolveBaseFileName(String tenTepRequest, String originalFilename) {
		if (StringUtils.hasText(tenTepRequest)) {
			return tenTepRequest.trim();
		}
		if (!StringUtils.hasText(originalFilename)) {
			return "tep-tin-" + System.currentTimeMillis();
		}
		String trimmedFileName = originalFilename.trim();
		if (!trimmedFileName.contains(".")) {
			return trimmedFileName;
		}
		return trimmedFileName.substring(0, trimmedFileName.lastIndexOf('.'));
	}

	private String normalizeSegment(String text) {
		String normalizedText = StringUtils.hasText(text) ? text.trim() : "tep-tin";
		String normalizedWithoutAccent = Normalizer.normalize(normalizedText, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		String slug = normalizedWithoutAccent.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
		if (!StringUtils.hasText(slug)) {
			throw new InvalidFieldException("Gia tri thuMuc/tenTep khong hop le");
		}
		return slug;
	}

	private String resolveLoaiTepTin(MultipartFile file, FileType fileType, String extension) {
		if (StringUtils.hasText(file.getContentType())) {
			return file.getContentType();
		}
		if (StringUtils.hasText(extension) && !"bin".equals(extension)) {
			return fileType.name().toLowerCase(Locale.ROOT) + "/" + extension;
		}
		return fileType.name().toLowerCase(Locale.ROOT);
	}
}
