package com.backend.cuutro.dto.request;

import jakarta.validation.constraints.Min;
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
public class TaoPhieuHoTroTepTinRequest {

	@NotNull(message = "tepTinId is required")
	Long tepTinId;

	@NotBlank(message = "loai is required")
	String loai;

	@Min(value = 0, message = "thuTu must be greater than or equal to 0")
	Integer thuTu;

	String moTa;
}
