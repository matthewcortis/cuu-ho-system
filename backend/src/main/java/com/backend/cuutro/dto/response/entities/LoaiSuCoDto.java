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
 * DTO for {@link com.backend.cuutro.entities.LoaiSuCoEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class LoaiSuCoDto implements Serializable {
	Long id;
	String ten;
	String iconUrl;
	String moTa;
	Boolean trangThai;
	@Builder.Default
	List<NhomVatPhamLiteDto> nhomVatPhams = new ArrayList<>();
	Instant createdAt;

}
