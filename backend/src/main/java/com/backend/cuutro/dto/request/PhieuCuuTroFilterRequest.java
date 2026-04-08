package com.backend.cuutro.dto.request;

import java.time.Instant;
import java.util.UUID;

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
public class PhieuCuuTroFilterRequest {

	private String hoTen;
	private String sdt;
	private String trangThai;
	private Long loaiSuCoId;
	private Long viTriId;
	private Long tepTinId;
	private UUID nguoiDungId;
	private Instant createdFrom;
	private Instant createdTo;
}
