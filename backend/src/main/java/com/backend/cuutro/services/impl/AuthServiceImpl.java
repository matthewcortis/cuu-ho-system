package com.backend.cuutro.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.RoleType;
import com.backend.cuutro.dto.request.DangNhapRequest;
import com.backend.cuutro.dto.request.DangKyRequest;
import com.backend.cuutro.dto.request.QuenMatKhauRequest;
import com.backend.cuutro.dto.response.entities.DangNhapResponse;
import com.backend.cuutro.dto.response.entities.DangKyResponse;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TaiKhoanEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.TaiKhoanRepository;
import com.backend.cuutro.services.AuthService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

	private static final String TOKEN_TYPE = "Bearer";
	private static final long ACCESS_TOKEN_EXPIRES_SECONDS = 24 * 60 * 60;

	private final TaiKhoanRepository taiKhoanRepository;
	private final NguoiDungRepository nguoiDungRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${constant.key.signer-key:}")
	private String signerKey;

	@Override
	public DangNhapResponse dangNhap(DangNhapRequest request) {
		String tenDangNhap = request.getTenDangNhap().trim();
		TaiKhoanEntity taiKhoan = taiKhoanRepository
				.findByTenDangNhapIgnoreCaseOrEmailIgnoreCase(tenDangNhap, tenDangNhap)
				.orElseThrow(() -> new BadCredentialsException("Ten dang nhap hoac mat khau khong dung"));

		if (!Boolean.TRUE.equals(taiKhoan.getTrangThai())) {
			throw new DisabledException("Tai khoan da bi khoa");
		}

		if (!kiemTraMatKhau(request.getMatKhau(), taiKhoan.getMatKhau())) {
			throw new BadCredentialsException("Ten dang nhap hoac mat khau khong dung");
		}

		RoleType vaiTro = taiKhoan.getVaiTro() == null ? RoleType.NGUOI_DAN : taiKhoan.getVaiTro();
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plusSeconds(ACCESS_TOKEN_EXPIRES_SECONDS);
		String accessToken = taoJwtToken(taiKhoan, vaiTro, issuedAt, expiresAt);

		return DangNhapResponse.builder()
				.tokenType(TOKEN_TYPE)
				.accessToken(accessToken)
				.expiresAt(expiresAt)
				.taiKhoanId(taiKhoan.getId())
				.tenDangNhap(taiKhoan.getTenDangNhap())
				.vaiTro(vaiTro)
				.build();
	}

	@Override
	@Transactional
	public DangKyResponse dangKy(DangKyRequest request) {
		String ten = normalizeRequired(request.getTen(), "ten is required");
		String tenDangNhap = normalizeRequired(request.getTenDangNhap(), "tenDangNhap is required");
		String email = normalizeRequired(request.getEmail(), "email is required")
				.toLowerCase(Locale.ROOT);
		String matKhau = normalizeRequired(request.getMatKhau(), "matKhau is required");

		if (matKhau.length() < 6) {
			throw new InvalidFieldException("matKhau must be at least 6 characters");
		}

		taiKhoanRepository.findByTenDangNhapIgnoreCase(tenDangNhap)
				.ifPresent(existing -> {
					throw new InvalidFieldException("Ten dang nhap da ton tai");
				});
		taiKhoanRepository.findByEmailIgnoreCase(email)
				.ifPresent(existing -> {
					throw new InvalidFieldException("Email da duoc su dung");
				});

		TaiKhoanEntity taiKhoanMoi = taiKhoanRepository.save(
				TaiKhoanEntity.builder()
						.tenDangNhap(tenDangNhap)
						.email(email)
						.matKhau(passwordEncoder.encode(matKhau))
						.trangThai(true)
						.vaiTro(RoleType.NGUOI_DAN)
						.build());

		nguoiDungRepository.save(
				NguoiDungEntity.builder()
						.taiKhoan(taiKhoanMoi)
						.ten(ten)
						.build());

		return DangKyResponse.builder()
				.taiKhoanId(taiKhoanMoi.getId())
				.tenDangNhap(taiKhoanMoi.getTenDangNhap())
				.email(taiKhoanMoi.getEmail())
				.vaiTro(taiKhoanMoi.getVaiTro())
				.build();
	}

	@Override
	@Transactional
	public void quenMatKhau(QuenMatKhauRequest request) {
		String email = normalizeRequired(request.getEmail(), "email is required")
				.toLowerCase(Locale.ROOT);
		String matKhauMoi = normalizeRequired(request.getMatKhauMoi(), "matKhauMoi is required");

		if (matKhauMoi.length() < 6) {
			throw new InvalidFieldException("matKhauMoi must be at least 6 characters");
		}

		TaiKhoanEntity taiKhoan = taiKhoanRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new InvalidFieldException("Khong tim thay tai khoan voi email nay"));

		taiKhoan.setMatKhau(passwordEncoder.encode(matKhauMoi));
		taiKhoanRepository.save(taiKhoan);
	}

	private String taoJwtToken(TaiKhoanEntity taiKhoan, RoleType vaiTro, Instant issuedAt, Instant expiresAt) {
		if (!StringUtils.hasText(signerKey)) {
			throw new IllegalStateException("Property constant.key.signer-key is not configured");
		}

		SecretKey secretKey = Keys.hmacShaKeyFor(signerKey.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
				.setSubject(String.valueOf(taiKhoan.getId()))
				.setIssuedAt(Date.from(issuedAt))
				.setExpiration(Date.from(expiresAt))
				.claim("taiKhoanId", taiKhoan.getId())
				.claim("tenDangNhap", taiKhoan.getTenDangNhap())
				.claim("roles", List.of(vaiTro.name()))
				.signWith(secretKey, SignatureAlgorithm.HS256)
				.compact();
	}

	private boolean kiemTraMatKhau(String rawPassword, String storedPassword) {
		if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
			return false;
		}
		try {
			if (passwordEncoder.matches(rawPassword, storedPassword)) {
				return true;
			}
		} catch (IllegalArgumentException ignored) {
			// Backward compatibility for plain-text passwords in old data.
		}
		return rawPassword.equals(storedPassword);
	}

	private String normalizeRequired(String value, String message) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(message);
		}
		return value.trim();
	}
}
