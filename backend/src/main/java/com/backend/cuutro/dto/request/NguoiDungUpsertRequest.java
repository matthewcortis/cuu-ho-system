package com.backend.cuutro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class NguoiDungUpsertRequest {

	@NotNull(message = "taiKhoanId is required")
	Long taiKhoanId;

	@NotBlank(message = "ten is required")
	String ten;

	@Email(message = "email is invalid")
	String email;

	@NotBlank(message = "sdt is required")
	@Pattern(regexp = "^\\d{10}$", message = "sdt must be 10 digits")
	String sdt;

	@Valid
	ViTriInputRequest viTri;

	String avatarUrl;
}
