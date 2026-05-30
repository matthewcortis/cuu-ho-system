package com.backend.cuutro.services;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.BangTinCreateRequest;
import com.backend.cuutro.dto.response.entities.BangTinDto;
import com.backend.cuutro.entities.BangTinEntity;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TepTinEntity;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.BangTinMapper;
import com.backend.cuutro.repository.BangTinRepository;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.TepTinRepository;
import com.backend.cuutro.repository.ViTriRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BangTinService {

	private final BangTinRepository bangTinRepository;
	private final NguoiDungRepository nguoiDungRepository;
	private final TepTinRepository tepTinRepository;
	private final ViTriRepository viTriRepository;
	private final ViTriCommandService viTriCommandService;
	private final BangTinMapper bangTinMapper;

	@Transactional
	public BangTinDto create(BangTinCreateRequest request) {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		NguoiDungEntity nguoiDung = nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId).orElse(null);

		BangTinEntity entity = new BangTinEntity();
		entity.setTieuDe(normalizeRequired(request.getTieuDe(), "tieuDe is required"));
		entity.setNoiDung(normalizeRequired(request.getNoiDung(), "noiDung is required"));
		entity.setTepTin(resolveTepTin(request.getTepTinId()));
		entity.setViTri(resolveViTri(request));
		entity.setNguoiDung(nguoiDung);
		entity.setTrangThai(Boolean.TRUE);

		return bangTinMapper.toDto(bangTinRepository.save(entity));
	}

	public List<BangTinDto> getDanhSachCongKhai() {
		return bangTinMapper.toDtoList(bangTinRepository.findByTrangThaiTrueOrderByCreatedAtDesc());
	}

	public List<BangTinDto> getDanhSachQuanLy() {
		return bangTinMapper.toDtoList(bangTinRepository.findAllByOrderByCreatedAtDesc());
	}

	public BangTinDto getByIdCongKhai(Long id) {
		BangTinEntity entity = bangTinRepository.findByIdAndTrangThaiTrue(id)
				.orElseThrow(() -> new EntityNotFoundException("BangTin not found with id=" + id));
		return bangTinMapper.toDto(entity);
	}

	@Transactional
	public void xoaBangTin(Long id) {
		BangTinEntity entity = bangTinRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("BangTin not found with id=" + id));
		bangTinRepository.delete(entity);
	}

	private TepTinEntity resolveTepTin(Long tepTinId) {
		if (tepTinId == null) {
			return null;
		}
		return tepTinRepository.findById(tepTinId)
				.orElseThrow(() -> new EntityNotFoundException("TepTin not found with id=" + tepTinId));
	}

	private ViTriEntity resolveViTri(BangTinCreateRequest request) {
		if (request.getViTriId() != null) {
			return viTriRepository.findById(request.getViTriId())
					.orElseThrow(() -> new EntityNotFoundException("ViTri not found with id=" + request.getViTriId()));
		}
		if (request.getViTri() != null) {
			return viTriCommandService.taoViTriMoi(request.getViTri());
		}
		return null;
	}

	private Long getCurrentTaiKhoanIdOrThrow() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BadCredentialsException("Unauthorized");
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof Jwt jwt) {
			Long fromClaim = convertToLong(jwt.getClaims().get("taiKhoanId"));
			if (fromClaim != null) {
				return fromClaim;
			}
			Long fromSubject = convertToLong(jwt.getSubject());
			if (fromSubject != null) {
				return fromSubject;
			}
			throw new BadCredentialsException("Invalid token: missing taiKhoanId");
		}

		if (principal instanceof Map<?, ?> claims) {
			Long fromClaim = convertToLong(claims.get("taiKhoanId"));
			if (fromClaim != null) {
				return fromClaim;
			}
		}

		throw new BadCredentialsException("Unauthorized");
	}

	private Long convertToLong(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.longValue();
		}
		String raw = String.valueOf(value).trim();
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		try {
			return Long.parseLong(raw);
		} catch (NumberFormatException ignored) {
			return null;
		}
	}

	private String normalizeRequired(String value, String errorMessage) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(errorMessage);
		}
		return value.trim();
	}
}
