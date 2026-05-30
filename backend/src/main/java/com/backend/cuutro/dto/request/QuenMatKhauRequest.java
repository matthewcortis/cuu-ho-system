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
public class QuenMatKhauRequest {

	@NotBlank(message = "email is required")
	@Email(message = "email is invalid")
	String email;

	@NotBlank(message = "matKhauMoi is required")
	@Size(min = 6, message = "matKhauMoi must be at least 6 characters")
	String matKhauMoi;
}
