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
@Table(name = "phieu_cuu_tro_tep_tin")
public class PhieuCuuTroTepTinEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "phieu_cuu_tro_id", nullable = false)
	private PhieuCuuTroEntity phieuCuuTro;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tep_tin_id", nullable = false)
	private TepTinEntity tepTin;

	@Column(name = "loai", nullable = false)
	private String loai;

	@ColumnDefault("0")
	@Column(name = "thu_tu")
	private Integer thuTu;

	@Column(name = "mo_ta")
	private String moTa;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;
}
