package com.backend.cuutro.dto.request;

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
public class GanDoiNhomTinhNguyenVienRequest {

	@NotNull(message = "tinhNguyenVienId is required")
	Long tinhNguyenVienId;

	@NotNull(message = "doiNhomId is required")
	Long doiNhomId;

	String vaiTro;
}

