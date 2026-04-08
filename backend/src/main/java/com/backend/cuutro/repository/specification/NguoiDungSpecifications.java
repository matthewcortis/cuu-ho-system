package com.backend.cuutro.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.NguoiDungFilterRequest;
import com.backend.cuutro.entities.NguoiDungEntity;

import jakarta.persistence.criteria.JoinType;

public final class NguoiDungSpecifications {

	private NguoiDungSpecifications() {
	}

	public static Specification<NguoiDungEntity> withFilter(NguoiDungFilterRequest filter) {
		Specification<NguoiDungEntity> specification = (root, query, cb) -> cb.conjunction();
		if (filter == null) {
			return specification;
		}

		if (StringUtils.hasText(filter.getTen())) {
			specification = specification.and(tenContains(filter.getTen()));
		}
		if (StringUtils.hasText(filter.getSdt())) {
			specification = specification.and(sdtEquals(filter.getSdt()));
		}
		if (filter.getTaiKhoanId() != null) {
			specification = specification.and(hasTaiKhoanId(filter.getTaiKhoanId()));
		}
		if (filter.getViTriId() != null) {
			specification = specification.and(hasViTriId(filter.getViTriId()));
		}
		if (filter.getCreatedFrom() != null) {
			specification = specification.and(createdAfterOrAt(filter.getCreatedFrom()));
		}
		if (filter.getCreatedTo() != null) {
			specification = specification.and(createdBeforeOrAt(filter.getCreatedTo()));
		}
		return specification;
	}

	public static Specification<NguoiDungEntity> tenContains(String keyword) {
		return (root, query, cb) -> cb.like(cb.lower(root.get("ten")), "%" + keyword.trim().toLowerCase() + "%");
	}

	public static Specification<NguoiDungEntity> sdtEquals(String sdt) {
		return (root, query, cb) -> cb.equal(root.get("sdt"), sdt.trim());
	}

	public static Specification<NguoiDungEntity> hasTaiKhoanId(Long taiKhoanId) {
		return (root, query, cb) -> cb.equal(root.join("taiKhoan", JoinType.LEFT).get("id"), taiKhoanId);
	}

	public static Specification<NguoiDungEntity> hasViTriId(Long viTriId) {
		return (root, query, cb) -> cb.equal(root.join("viTri", JoinType.LEFT).get("id"), viTriId);
	}

	public static Specification<NguoiDungEntity> createdAfterOrAt(java.time.Instant from) {
		return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
	}

	public static Specification<NguoiDungEntity> createdBeforeOrAt(java.time.Instant to) {
		return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
	}
}
