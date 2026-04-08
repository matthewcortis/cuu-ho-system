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
 * DTO for {@link com.backend.cuutro.entities.DoiNhomEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class DoiNhomDto implements Serializable {
	Long id;
	String tenDoiNhom;
	String soDienThoai;
	ViTriDto viTri;
	Boolean trangThaiHoatDong;
	Boolean active;
	String trangThai;
	Instant createdAt;

}
