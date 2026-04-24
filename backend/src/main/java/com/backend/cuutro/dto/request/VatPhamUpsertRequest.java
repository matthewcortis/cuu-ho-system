package com.backend.cuutro.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

	@NotEmpty(message = "nhomVatPhamIds is required")
	List<Long> nhomVatPhamIds;

	Long tepTinId;

	Boolean trangThai;
}
