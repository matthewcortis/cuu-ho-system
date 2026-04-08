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

/**
 * DTO for {@link com.backend.cuutro.entities.ChiTietCuuTroEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class ChiTietCuuTroDto implements Serializable {
	Long id;
	PhieuCuuTroDto phieuCuuTro;
	VatPhamDto vatPham;
	Integer soLuong;
	String ghiChu;
	LocalDateTime createdAt;

}
