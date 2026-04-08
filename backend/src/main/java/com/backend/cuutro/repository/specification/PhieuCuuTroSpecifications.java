package com.backend.cuutro.repository.specification;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.PhieuCuuTroFilterRequest;
import com.backend.cuutro.entities.PhieuCuuTroEntity;

import jakarta.persistence.criteria.JoinType;

public final class PhieuCuuTroSpecifications {

	private PhieuCuuTroSpecifications() {
	}

	public static Specification<PhieuCuuTroEntity> withFilter(PhieuCuuTroFilterRequest filter) {
		Specification<PhieuCuuTroEntity> specification = (root, query, cb) -> cb.conjunction();
		if (filter == null) {
			return specification;
		}

		if (StringUtils.hasText(filter.getHoTen())) {
			specification = specification.and(hoTenContains(filter.getHoTen()));
		}
		if (StringUtils.hasText(filter.getSdt())) {
			specification = specification.and(sdtEquals(filter.getSdt()));
		}
		if (StringUtils.hasText(filter.getTrangThai())) {
			specification = specification.and(hasTrangThai(filter.getTrangThai()));
		}
		if (filter.getLoaiSuCoId() != null) {
			specification = specification.and(hasLoaiSuCoId(filter.getLoaiSuCoId()));
		}
		if (filter.getViTriId() != null) {
			specification = specification.and(hasViTriId(filter.getViTriId()));
		}
		if (filter.getTepTinId() != null) {
			specification = specification.and(hasTepTinId(filter.getTepTinId()));
		}
		if (filter.getNguoiDungId() != null) {
			specification = specification.and(hasNguoiDungId(filter.getNguoiDungId()));
		}
		if (filter.getCreatedFrom() != null) {
			specification = specification.and(createdAfterOrAt(filter.getCreatedFrom()));
		}
		if (filter.getCreatedTo() != null) {
			specification = specification.and(createdBeforeOrAt(filter.getCreatedTo()));
		}
		return specification;
	}

	public static Specification<PhieuCuuTroEntity> hoTenContains(String keyword) {
		return (root, query, cb) -> cb.like(cb.lower(root.get("hoTen")), "%" + keyword.trim().toLowerCase() + "%");
	}

	public static Specification<PhieuCuuTroEntity> sdtEquals(String sdt) {
		return (root, query, cb) -> cb.equal(root.get("sdt"), sdt.trim());
	}

	public static Specification<PhieuCuuTroEntity> hasTrangThai(String trangThai) {
		return (root, query, cb) -> cb.equal(cb.lower(root.get("trangThai")), trangThai.trim().toLowerCase());
	}

	public static Specification<PhieuCuuTroEntity> hasLoaiSuCoId(Long loaiSuCoId) {
		return (root, query, cb) -> cb.equal(root.join("loaiSuCo", JoinType.LEFT).get("id"), loaiSuCoId);
	}

	public static Specification<PhieuCuuTroEntity> hasViTriId(Long viTriId) {
		return (root, query, cb) -> cb.equal(root.join("viTri", JoinType.LEFT).get("id"), viTriId);
	}

	public static Specification<PhieuCuuTroEntity> hasTepTinId(Long tepTinId) {
		return (root, query, cb) -> cb.equal(root.join("tepTin", JoinType.LEFT).get("id"), tepTinId);
	}

	public static Specification<PhieuCuuTroEntity> hasNguoiDungId(UUID nguoiDungId) {
		return (root, query, cb) -> cb.equal(root.join("nguoiDung", JoinType.LEFT).get("id"), nguoiDungId);
	}

	public static Specification<PhieuCuuTroEntity> createdAfterOrAt(Instant from) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
	}

	public static Specification<PhieuCuuTroEntity> createdBeforeOrAt(Instant to) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
	}
}
