package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * DTO for {@link com.backend.cuutro.entities.SoLuongVatPhamEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class SoLuongVatPhamDto implements Serializable {
	Long id;
	KhaiBaoDto khaiBao;
	VatPhamDto vatPham;
	Short soLuong;
	Instant createdAt;

}
