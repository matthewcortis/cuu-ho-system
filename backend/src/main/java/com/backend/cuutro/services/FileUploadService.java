package com.backend.cuutro.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

	String uploadFile(MultipartFile file, String folder, String publicId);

	default String uploadFile(
			MultipartFile file,
			String folder,
			String publicId,
			String resourceType,
			String format) {
		return uploadFile(file, folder, publicId);
	}
}
