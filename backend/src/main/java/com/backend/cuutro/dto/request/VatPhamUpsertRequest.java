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
public class VatPhamUpsertRequest {

	@NotBlank(message = "tenVatPham is required")
	String tenVatPham;

	@NotNull(message = "soLuong is required")
	@Min(value = 0, message = "soLuong must be greater than or equal to 0")
	Short soLuong;

	@NotNull(message = "donViId is required")
	Long donViId;

	@NotNull(message = "nhomVatPhamId is required")
	Long nhomVatPhamId;

	Long tepTinId;

	Boolean trangThai;
}

