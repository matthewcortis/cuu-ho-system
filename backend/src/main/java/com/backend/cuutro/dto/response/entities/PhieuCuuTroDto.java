package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * DTO for {@link com.backend.cuutro.entities.PhieuCuuTroEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class PhieuCuuTroDto implements Serializable {
	Long id;
	LoaiSuCoDto loaiSuCo;
	ViTriDto viTri;
	List<PhieuCuuTroTepTinDto> tepTins;
	NguoiGuiDto nguoiGui;
	String ghiChu;
	String trangThai;
	PhanCongDto phanCong;
	List<PhieuCuuTroChiTietDto> chiTietCuuTro;
	Instant createdAt;

}
