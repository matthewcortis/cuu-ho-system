package com.backend.cuutro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoiNhomTaoRequest {

	@NotBlank(message = "tenDoiNhom is required")
	String tenDoiNhom;

	@NotBlank(message = "soDienThoai is required")
	String soDienThoai;

	@NotNull(message = "viTriId is required")
	Long viTriId;

	@NotNull(message = "doiTruongTinhNguyenVienId is required")
	Long doiTruongTinhNguyenVienId;
}

