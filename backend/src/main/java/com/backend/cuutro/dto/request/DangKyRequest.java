package com.backend.cuutro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class DangKyRequest {

	@NotBlank(message = "ten is required")
	String ten;

	@NotBlank(message = "tenDangNhap is required")
	String tenDangNhap;

	@NotBlank(message = "email is required")
	@Email(message = "email is invalid")
	String email;

	@NotBlank(message = "matKhau is required")
	@Size(min = 6, message = "matKhau must be at least 6 characters")
	String matKhau;
}
