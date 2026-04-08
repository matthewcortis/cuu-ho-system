package com.backend.cuutro.entities;

import java.io.Serializable;
import java.time.Instant;

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
@Table(name = "doi_nhom")
public class DoiNhomEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "ten_doi_nhom")
	private String tenDoiNhom;

	@Column(name = "so_dien_thoai")
	private String soDienThoai;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vi_tri_id")
	private ViTriEntity viTri;

	@ColumnDefault("true")
	@Column(name = "trang_thai_hoat_dong")
	private Boolean trangThaiHoatDong;

	@ColumnDefault("true")
	@Column(name = "active")
	private Boolean active;

	@ColumnDefault("'idle'")
	@Column(name = "trang_thai")
	private String trangThai;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;
}
