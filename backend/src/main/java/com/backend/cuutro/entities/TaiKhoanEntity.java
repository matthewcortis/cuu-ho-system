package com.backend.cuutro.entities;

import java.io.Serializable;
import java.time.Instant;

import com.backend.cuutro.config.converter.RoleTypeConverter;
import com.backend.cuutro.constant.enums.RoleType;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tai_khoan")
public class TaiKhoanEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "email")
	private String email;

	@Column(name = "ten_dang_nhap")
	private String tenDangNhap;

	@Column(name = "mat_khau")
	private String matKhau;

	@ColumnDefault("true")
	@Column(name = "trang_thai")
	private Boolean trangThai;

	@Convert(converter = RoleTypeConverter.class)
	@ColumnDefault("'NGUOI_DAN'")
	@Column(name = "vai_tro")
	private RoleType vaiTro;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

}
