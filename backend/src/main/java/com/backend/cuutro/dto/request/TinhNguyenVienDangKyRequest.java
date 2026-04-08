package com.backend.cuutro.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

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
public class TinhNguyenVienDangKyRequest {

	@NotNull(message = "nguoiDungId is required")
	UUID nguoiDungId;

	LocalDateTime thoiGian;

	String ghiChu;

	String coTheGiup;
}

