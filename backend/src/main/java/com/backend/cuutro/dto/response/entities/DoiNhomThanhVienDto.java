package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"tinhNguyenVienId", "vaiTro"})
public class DoiNhomThanhVienDto implements Serializable {
	Long tinhNguyenVienId;
	String ten;
	String sdt;
	String avatarUrl;
	ViTriDto viTri;
	String vaiTro;
}
