package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
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
 * DTO for {@link com.backend.cuutro.entities.VatPhamEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class VatPhamDto implements Serializable {
	Long id;
	String tenVatPham;
	Short soLuong;
	DonViDto donVi;
	@Builder.Default
	List<NhomVatPhamLiteDto> nhomVatPhams = new ArrayList<>();
	TepTinDto tepTin;
	Boolean trangThai;
	Instant createdAt;

}
