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

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode
public class DangNhapResponse implements Serializable {

	String tokenType;
	String accessToken;
	Instant expiresAt;
	Long taiKhoanId;
	String tenDangNhap;
	RoleType vaiTro;
}

