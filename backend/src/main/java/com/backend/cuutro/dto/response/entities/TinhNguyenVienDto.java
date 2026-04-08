package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.Instant;
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
 * DTO for {@link com.backend.cuutro.entities.TinhNguyenVienEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class TinhNguyenVienDto implements Serializable {
	Long id;
	NguoiDungDto nguoiDung;
	LocalDateTime thoiGian;
	String ghiChu;
	String coTheGiup;
	String trangThaiDuyet;
	LocalDateTime thoiGianDuyet;
	Instant createdAt;

}
