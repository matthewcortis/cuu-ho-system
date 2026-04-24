package com.backend.cuutro.entities;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "tin_nhan_da_xem", uniqueConstraints = {
		@UniqueConstraint(name = "uk_tin_nhan_da_xem_phieu_nguoi_dung", columnNames = { "phieu_cuu_tro_id", "nguoi_dung_id" })
})
public class TinNhanDaXemEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "phieu_cuu_tro_id", nullable = false)
	private PhieuCuuTroEntity phieuCuuTro;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nguoi_dung_id", nullable = false)
	private NguoiDungEntity nguoiDung;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "last_seen_message_id")
	private TinNhanEntity lastSeenMessage;

	@Column(name = "last_seen_at")
	private Instant lastSeenAt;

	@CreationTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@ColumnDefault("CURRENT_TIMESTAMP")
	@Column(name = "updated_at")
	private Instant updatedAt;
}

