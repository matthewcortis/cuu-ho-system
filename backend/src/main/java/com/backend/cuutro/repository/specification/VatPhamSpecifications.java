package com.backend.cuutro.repository.specification;

import java.time.Instant;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.VatPhamFilterRequest;
import com.backend.cuutro.entities.VatPhamEntity;

import jakarta.persistence.criteria.JoinType;

public final class VatPhamSpecifications {

	private VatPhamSpecifications() {
	}

	public static Specification<VatPhamEntity> withFilter(VatPhamFilterRequest filter) {
		Specification<VatPhamEntity> specification = (root, query, cb) -> cb.conjunction();
		if (filter == null) {
			return specification;
		}

		if (StringUtils.hasText(filter.getTenVatPham())) {
			specification = specification.and(tenVatPhamContains(filter.getTenVatPham()));
		}
		if (filter.getSoLuongMin() != null) {
			specification = specification.and(soLuongGreaterThanOrEqual(filter.getSoLuongMin()));
		}
		if (filter.getSoLuongMax() != null) {
			specification = specification.and(soLuongLessThanOrEqual(filter.getSoLuongMax()));
		}
		if (filter.getDonViId() != null) {
			specification = specification.and(hasDonViId(filter.getDonViId()));
		}
		if (filter.getNhomVatPhamId() != null) {
			specification = specification.and(hasNhomVatPhamId(filter.getNhomVatPhamId()));
		}
		if (filter.getTrangThai() != null) {
			specification = specification.and(hasTrangThai(filter.getTrangThai()));
		}
		if (filter.getCreatedFrom() != null) {
			specification = specification.and(createdAfterOrAt(filter.getCreatedFrom()));
		}
		if (filter.getCreatedTo() != null) {
			specification = specification.and(createdBeforeOrAt(filter.getCreatedTo()));
		}
		return specification;
	}

	public static Specification<VatPhamEntity> tenVatPhamContains(String keyword) {
		return (root, query, cb) -> cb.like(cb.lower(root.get("tenVatPham")), "%" + keyword.trim().toLowerCase() + "%");
	}

	public static Specification<VatPhamEntity> soLuongGreaterThanOrEqual(Short soLuongMin) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("soLuong"), soLuongMin);
	}

	public static Specification<VatPhamEntity> soLuongLessThanOrEqual(Short soLuongMax) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("soLuong"), soLuongMax);
	}

	public static Specification<VatPhamEntity> hasDonViId(Long donViId) {
		return (root, query, cb) -> cb.equal(root.join("donVi", JoinType.LEFT).get("id"), donViId);
	}

	public static Specification<VatPhamEntity> hasNhomVatPhamId(Long nhomVatPhamId) {
		return (root, query, cb) -> cb.equal(root.join("nhomVatPham", JoinType.LEFT).get("id"), nhomVatPhamId);
	}

	public static Specification<VatPhamEntity> hasTrangThai(Boolean trangThai) {
		return (root, query, cb) -> cb.equal(root.get("trangThai"), trangThai);
	}

	public static Specification<VatPhamEntity> createdAfterOrAt(Instant from) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
	}

	public static Specification<VatPhamEntity> createdBeforeOrAt(Instant to) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
	}
}
