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
@Table(name = "phieu_cuu_tro")
public class PhieuCuuTroEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "loai_su_co_id")
	private LoaiSuCoEntity loaiSuCo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vi_tri_id")
	private ViTriEntity viTri;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tep_tin_id")
	private TepTinEntity tepTin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nguoi_dung_id")
	private NguoiDungEntity nguoiDung;

	@Column(name = "ho_ten")
	private String hoTen;

	@Column(name = "sdt")
	private String sdt;

	@Column(name = "ghi_chu")
	private String ghiChu;

	@ColumnDefault("'pending'")
	@Column(name = "trang_thai")
	private String trangThai;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;
}
