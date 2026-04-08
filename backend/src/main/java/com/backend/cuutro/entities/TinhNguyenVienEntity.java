package com.backend.cuutro.entities;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tinh_nguyen_vien")
public class TinhNguyenVienEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nguoi_dung_id")
	private NguoiDungEntity nguoiDung;

	@Column(name = "thoi_gian")
	private LocalDateTime thoiGian;

	@Column(name = "ghi_chu")
	private String ghiChu;

	@Column(name = "co_the_giup")
	private String coTheGiup;

	@ColumnDefault("'CHO_XET_DUYET'")
	@Column(name = "trang_thai_duyet")
	private String trangThaiDuyet;

	@Column(name = "thoi_gian_duyet")
	private LocalDateTime thoiGianDuyet;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;
}
