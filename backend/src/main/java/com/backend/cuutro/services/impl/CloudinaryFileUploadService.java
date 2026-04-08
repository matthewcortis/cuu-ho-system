package com.backend.cuutro.services.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.services.FileUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryFileUploadService implements FileUploadService {

	private final Cloudinary cloudinary;

	public CloudinaryFileUploadService(@Value("${cloudinary.url}") String cloudinaryUrl) {
		if (StringUtils.hasText(cloudinaryUrl)) {
			this.cloudinary = new Cloudinary(cloudinaryUrl);
			this.cloudinary.config.secure = true;
			return;
		}
		this.cloudinary = null;
	}

	@Override
	public String uploadFile(MultipartFile file, String folder, String publicId) {
		return uploadFile(file, folder, publicId, "image", null);
	}

	@Override
	public String uploadFile(MultipartFile file, String folder, String publicId, String resourceType, String format) {
		if (cloudinary == null) {
			throw new InvalidFieldException("Cloudinary chua duoc cau hinh. Vui long set CLOUDINARY_URL");
		}
		if (file == null || file.isEmpty()) {
			throw new InvalidFieldException("tepTin is required");
		}

		try {
			String finalResourceType = StringUtils.hasText(resourceType) ? resourceType.trim() : "image";
			Map<String, Object> uploadOptions = new HashMap<>(ObjectUtils.asMap(
					"resource_type", finalResourceType,
					"folder", folder,
					"public_id", publicId,
					"overwrite", true));
			if (StringUtils.hasText(format)) {
				uploadOptions.put("format", format.toLowerCase(Locale.ROOT));
			}

			Map<?, ?> uploaded = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
			Object secureUrl = uploaded.get("secure_url");
			if (secureUrl == null) {
				throw new IllegalStateException("Cloudinary upload khong tra ve secure_url");
			}
			return secureUrl.toString();
		} catch (IOException exception) {
			throw new IllegalStateException("Khong the upload anh len Cloudinary", exception);
		}
	}
}
