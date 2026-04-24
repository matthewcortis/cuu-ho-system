package com.backend.cuutro.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "nhom_vat_pham")
public class NhomVatPhamEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "ten")
	private String ten;

	@Column(name = "mo_ta")
	private String moTa;

	@Builder.Default
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "nhom_vat_pham_loai_su_co",
			joinColumns = @JoinColumn(name = "nhom_vat_pham_id"),
			inverseJoinColumns = @JoinColumn(name = "loai_su_co_id"))
	private Set<LoaiSuCoEntity> loaiSuCos = new LinkedHashSet<>();

	@Builder.Default
	@ManyToMany(mappedBy = "nhomVatPhams", fetch = FetchType.LAZY)
	private Set<VatPhamEntity> vatPhams = new LinkedHashSet<>();

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;
}
