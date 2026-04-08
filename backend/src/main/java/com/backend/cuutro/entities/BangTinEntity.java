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
@Table(name = "bang_tin")
public class BangTinEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "tieu_de")
	private String tieuDe;

	@Column(name = "noi_dung")
	private String noiDung;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tep_tin_id")
	private TepTinEntity tepTin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vi_tri_id")
	private ViTriEntity viTri;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nguoi_dung_id")
	private NguoiDungEntity nguoiDung;

	@Column(name = "trang_thai")
	private Boolean trangThai;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;
}
