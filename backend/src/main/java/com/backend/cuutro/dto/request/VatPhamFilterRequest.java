package com.backend.cuutro.dto.request;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VatPhamFilterRequest {

	private String tenVatPham;
	private Short soLuongMin;
	private Short soLuongMax;
	private Long donViId;
	private Long nhomVatPhamId;
	private Boolean trangThai;
	private Instant createdFrom;
	private Instant createdTo;
}
