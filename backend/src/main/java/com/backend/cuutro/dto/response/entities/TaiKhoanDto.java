package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.Instant;

import com.backend.cuutro.constant.enums.RoleType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * DTO for {@link com.backend.cuutro.entities.TaiKhoanEntity}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = {"id"})
public class TaiKhoanDto implements Serializable {
	Long id;
	String email;
	String tenDangNhap;
	String matKhau;
	Boolean trangThai;
	RoleType vaiTro;
	Instant createdAt;

}
