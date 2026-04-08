package com.backend.cuutro.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class DangNhapRequest {

	@NotBlank(message = "tenDangNhap is required")
	String tenDangNhap;

	@NotBlank(message = "matKhau is required")
	String matKhau;
}

