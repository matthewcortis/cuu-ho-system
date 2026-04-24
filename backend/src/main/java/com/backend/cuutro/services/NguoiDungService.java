package com.backend.cuutro.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.backend.cuutro.dto.request.NguoiDungFilterRequest;
import com.backend.cuutro.dto.request.NguoiDungUpsertRequest;
import com.backend.cuutro.dto.request.ViTriInputRequest;
import com.backend.cuutro.dto.response.entities.NguoiDungDto;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TaiKhoanEntity;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.NguoiDungMapper;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.TaiKhoanRepository;
import com.backend.cuutro.repository.specification.NguoiDungSpecifications;
import com.backend.cuutro.services.FileUploadService;
import com.backend.cuutro.services.ViTriCommandService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NguoiDungService {

	private static final int MIN_PASSWORD_LENGTH = 6;

	private final NguoiDungRepository nguoiDungRepository;
	private final TaiKhoanRepository taiKhoanRepository;
	private final NguoiDungMapper nguoiDungMapper;
	private final ViTriCommandService viTriCommandService;
	private final FileUploadService fileUploadService;
	private final PasswordEncoder passwordEncoder;
	private final EntityManager entityManager;

	@Transactional
	public NguoiDungDto create(NguoiDungUpsertRequest request) {
		NguoiDungEntity entity = new NguoiDungEntity();
		applyRequest(entity, request, null);
		return nguoiDungMapper.toDto(nguoiDungRepository.save(entity));
	}

	@Transactional
	public NguoiDungDto update(UUID id, NguoiDungUpsertRequest request) {
		NguoiDungEntity entity = getEntityOrThrow(id);
		applyRequest(entity, request, null);
		return nguoiDungMapper.toDto(nguoiDungRepository.save(entity));
	}

	@Transactional
	public void delete(UUID id) {
		NguoiDungEntity entity = getEntityOrThrow(id);
		nguoiDungRepository.delete(entity);
	}

	public NguoiDungDto getById(UUID id) {
		return nguoiDungMapper.toDto(getEntityOrThrow(id));
	}

	public Page<NguoiDungEntity> search(NguoiDungFilterRequest filter, Pageable pageable) {
		return nguoiDungRepository.findAll(NguoiDungSpecifications.withFilter(filter), pageable);
	}

	public List<NguoiDungEntity> search(NguoiDungFilterRequest filter) {
		return nguoiDungRepository.findAll(NguoiDungSpecifications.withFilter(filter));
	}

	public List<NguoiDungDto> getDanhSach() {
		List<NguoiDungEntity> entities = nguoiDungRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
		return nguoiDungMapper.toDtoList(entities);
	}

	public NguoiDungDto getThongTinHienTai() {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		NguoiDungEntity entity = nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.orElseThrow(() -> new EntityNotFoundException("NguoiDung not found for current account"));
		return nguoiDungMapper.toDto(entity);
	}

	@Transactional
	public NguoiDungDto capNhatThongTinHienTai(NguoiDungUpsertRequest request) {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		NguoiDungEntity entity = nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.orElseGet(NguoiDungEntity::new);
		applyRequest(entity, request, taiKhoanId);
		return nguoiDungMapper.toDto(nguoiDungRepository.save(entity));
	}

	@Transactional
	public void xoaThongTinHienTai() {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.ifPresent(nguoiDungRepository::delete);
	}

	@Transactional
	public void doiMatKhauHienTai(String matKhauHienTai, String matKhauMoi) {
		String matKhauHienTaiDaChuanHoa = normalizePasswordRequired(matKhauHienTai, "matKhauHienTai is required");
		String matKhauMoiDaChuanHoa = normalizePasswordRequired(matKhauMoi, "matKhauMoi is required");
		if (matKhauMoiDaChuanHoa.length() < MIN_PASSWORD_LENGTH) {
			throw new InvalidFieldException("matKhauMoi must be at least 6 characters");
		}
		if (matKhauMoiDaChuanHoa.equals(matKhauHienTaiDaChuanHoa)) {
			throw new InvalidFieldException("Mat khau moi phai khac mat khau hien tai");
		}

		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		TaiKhoanEntity taiKhoan = taiKhoanRepository.findById(taiKhoanId)
				.orElseThrow(() -> new EntityNotFoundException("TaiKhoan not found with id=" + taiKhoanId));

		if (!kiemTraMatKhau(matKhauHienTaiDaChuanHoa, taiKhoan.getMatKhau())) {
			throw new InvalidFieldException("Mat khau hien tai khong dung");
		}
		taiKhoan.setMatKhau(passwordEncoder.encode(matKhauMoiDaChuanHoa));
		taiKhoanRepository.saveAndFlush(taiKhoan);
		entityManager.clear();

		TaiKhoanEntity taiKhoanSauCapNhat = taiKhoanRepository.findById(taiKhoanId)
				.orElseThrow(() -> new EntityNotFoundException("TaiKhoan not found after update with id=" + taiKhoanId));
		if (!kiemTraMatKhau(matKhauMoiDaChuanHoa, taiKhoanSauCapNhat.getMatKhau())) {
			throw new IllegalStateException("Khong the cap nhat mat khau tai khoan");
		}
	}

	@Transactional
	public NguoiDungDto capNhatAvatarHienTai(MultipartFile avatar) {
		validateAvatarFile(avatar);

		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		TaiKhoanEntity taiKhoan = taiKhoanRepository.findById(taiKhoanId)
				.orElseThrow(() -> new EntityNotFoundException("TaiKhoan not found with id=" + taiKhoanId));
		NguoiDungEntity entity = nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.orElseGet(() -> {
					NguoiDungEntity newEntity = new NguoiDungEntity();
					newEntity.setTaiKhoan(taiKhoan);
					return newEntity;
				});

		String uploadedUrl = fileUploadService.uploadFile(
				avatar,
				"nguoi-dung/avatar",
				"tai-khoan-" + taiKhoanId,
				"image",
				resolveImageExtension(avatar));
		entity.setAvatarUrl(uploadedUrl);
		return nguoiDungMapper.toDto(nguoiDungRepository.save(entity));
	}

	@Transactional
	public NguoiDungDto capNhatTrangThaiTaiKhoan(UUID nguoiDungId, Boolean trangThai) {
		NguoiDungEntity nguoiDung = getEntityOrThrow(nguoiDungId);

		TaiKhoanEntity taiKhoan = nguoiDung.getTaiKhoan();
		if (taiKhoan == null || taiKhoan.getId() == null) {
			throw new InvalidFieldException("Nguoi dung chua co tai khoan de cap nhat trang thai");
		}

		taiKhoan.setTrangThai(Boolean.TRUE.equals(trangThai));
		taiKhoanRepository.save(taiKhoan);
		nguoiDung.setTaiKhoan(taiKhoan);

		return nguoiDungMapper.toDto(nguoiDung);
	}

	private NguoiDungEntity getEntityOrThrow(UUID id) {
		return nguoiDungRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("NguoiDung not found with id=" + id));
	}

	private void applyRequest(NguoiDungEntity entity, NguoiDungUpsertRequest request, Long forcedTaiKhoanId) {
		Long taiKhoanId = forcedTaiKhoanId != null ? forcedTaiKhoanId : request.getTaiKhoanId();
		TaiKhoanEntity taiKhoan = taiKhoanRepository.findById(taiKhoanId)
				.orElseThrow(() -> new EntityNotFoundException("TaiKhoan not found with id=" + taiKhoanId));
		nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.filter(existing -> !existing.getId().equals(entity.getId()))
				.ifPresent(existing -> {
					throw new InvalidFieldException("Tai khoan da duoc lien ket voi nguoi dung khac");
				});

		entity.setTaiKhoan(taiKhoan);
		entity.setTen(normalizeRequired(request.getTen(), "ten is required"));
		entity.setSdt(normalizeRequired(request.getSdt(), "sdt is required"));
		entity.setAvatarUrl(normalizeNullable(request.getAvatarUrl()));
		String email = normalizeNullable(request.getEmail());
		if (email != null) {
			taiKhoan.setEmail(email);
		}

		ViTriInputRequest viTriRequest = request.getViTri();
		if (viTriRequest == null) {
			entity.setViTri(null);
			return;
		}

		ViTriEntity viTri = entity.getViTri();
		if (viTri == null) {
			entity.setViTri(viTriCommandService.taoViTriMoi(viTriRequest));
			return;
		}
		viTri.setDiaChi(normalizeRequired(viTriRequest.getDiaChi(), "viTri.diaChi is required"));
		viTri.setLat(normalizeNullable(viTriRequest.getLat()));
		viTri.setLongitude(normalizeNullable(viTriRequest.getLongitude()));
	}

	private Long getCurrentTaiKhoanIdOrThrow() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Unauthorized");
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Jwt jwt) {
			Long fromClaim = convertToLong(jwt.getClaims().get("taiKhoanId"));
			if (fromClaim != null) {
				return fromClaim;
			}
			Long fromSubject = convertToLong(jwt.getSubject());
			if (fromSubject != null) {
				return fromSubject;
			}
			throw new BadCredentialsException("Invalid token: missing taiKhoanId");
		}

		if (principal instanceof Map<?, ?> claims) {
			Long fromClaim = convertToLong(claims.get("taiKhoanId"));
			if (fromClaim != null) {
				return fromClaim;
			}
		}

		throw new BadCredentialsException("Unauthorized");
	}

	private Long convertToLong(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.longValue();
		}
		String raw = String.valueOf(value).trim();
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private boolean kiemTraMatKhau(String rawPassword, String storedPassword) {
		if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
			return false;
		}
		try {
			if (passwordEncoder.matches(rawPassword, storedPassword)) {
				return true;
			}
		} catch (IllegalArgumentException ignored) {
			// Backward compatibility for plain-text passwords in old data.
		}
		return rawPassword.equals(storedPassword);
	}

	private String normalizeRequired(String value, String errorMessage) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(errorMessage);
		}
		return value.trim();
	}

	private String normalizeNullable(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private String normalizePasswordRequired(String value, String errorMessage) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(errorMessage);
		}
		return value;
	}

	private void validateAvatarFile(MultipartFile avatar) {
		if (avatar == null || avatar.isEmpty()) {
			throw new InvalidFieldException("avatar is required");
		}

		long maxBytes = 8L * 1024 * 1024;
		if (avatar.getSize() > maxBytes) {
			throw new InvalidFieldException("Kich thuoc avatar vuot qua 8MB");
		}

		String contentType = normalizeNullable(avatar.getContentType());
		if (contentType != null && contentType.toLowerCase().startsWith("image/")) {
			return;
		}
		String extension = resolveImageExtension(avatar);
		if (extension == null) {
			throw new InvalidFieldException("avatar chi ho tro dinh dang image");
		}
	}

	private String resolveImageExtension(MultipartFile avatar) {
		String contentType = normalizeNullable(avatar.getContentType());
		if (contentType != null) {
			String normalized = contentType.toLowerCase();
			switch (normalized) {
				case "image/jpeg":
				case "image/jpg":
					return "jpg";
				case "image/png":
					return "png";
				case "image/webp":
					return "webp";
				case "image/gif":
					return "gif";
				case "image/bmp":
					return "bmp";
				default:
					break;
			}
		}

		String originalFileName = normalizeNullable(avatar.getOriginalFilename());
		if (originalFileName != null && originalFileName.contains(".")) {
			String ext = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();
			switch (ext) {
				case "jpg":
				case "jpeg":
					return "jpg";
				case "png":
				case "webp":
				case "gif":
				case "bmp":
					return ext;
				default:
					return null;
			}
		}
		return null;
	}
}
