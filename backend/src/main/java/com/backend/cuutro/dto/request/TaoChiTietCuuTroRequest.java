package com.backend.cuutro.dto.request;

import jakarta.validation.constraints.Min;
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
public class TaoChiTietCuuTroRequest {

	@NotNull(message = "vatPhamId is required")
	Long vatPhamId;

	@NotNull(message = "soLuong is required")
	@Min(value = 1, message = "soLuong must be greater than 0")
	Integer soLuong;

	String ghiChu;
}
