package com.backend.cuutro.entities;

import java.io.Serializable;
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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "chi_tiet_cuu_tro", uniqueConstraints = {
		@UniqueConstraint(name = "uk_chi_tiet_cuu_tro_phieu_vat_pham", columnNames = { "phieu_cuu_tro_id", "vat_pham_id" })
})
public class ChiTietCuuTroEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "phieu_cuu_tro_id", nullable = false)
	private PhieuCuuTroEntity phieuCuuTro;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vat_pham_id", nullable = false)
	private VatPhamEntity vatPham;

	@Column(name = "so_luong", nullable = false)
	private Integer soLuong;

	@Column(name = "ghi_chu")
	private String ghiChu;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
}
