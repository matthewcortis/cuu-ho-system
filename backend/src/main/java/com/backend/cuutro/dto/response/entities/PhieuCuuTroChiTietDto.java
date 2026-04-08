package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@EqualsAndHashCode(of = { "id" })
public class PhieuCuuTroChiTietDto implements Serializable {
	Long id;
	Long vatPhamId;
	String tenVatPham;
	Integer soLuong;
	String ghiChu;
	LocalDateTime createdAt;
}
