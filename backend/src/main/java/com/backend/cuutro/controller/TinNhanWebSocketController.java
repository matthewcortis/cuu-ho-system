package com.backend.cuutro.controller;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.CapNhatDaXemTinNhanRequest;
import com.backend.cuutro.dto.request.CapNhatTrangThaiDangGoTinNhanRequest;
import com.backend.cuutro.dto.request.GuiTinNhanPhieuRequest;
import com.backend.cuutro.services.PhieuCuuTroService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TinNhanWebSocketController {

	private final PhieuCuuTroService phieuCuuTroService;

	@MessageMapping("/phieu-cuu-tro/{id}/tin-nhan")
	public void guiTinNhan(
			@DestinationVariable("id") Long phieuCuuTroId,
			GuiTinNhanPhieuRequest request,
			Principal principal) {
		Authentication authentication = resolveAuthentication(principal);
		Long taiKhoanId = resolveTaiKhoanId(authentication);
		Set<String> roles = resolveRoles(authentication);
		phieuCuuTroService.guiTinNhan(phieuCuuTroId, request, taiKhoanId, roles);
	}

	@MessageMapping("/phieu-cuu-tro/{id}/tin-nhan/da-xem")
	public void capNhatDaXemTinNhan(
			@DestinationVariable("id") Long phieuCuuTroId,
			CapNhatDaXemTinNhanRequest request,
			Principal principal) {
		Authentication authentication = resolveAuthentication(principal);
		Long taiKhoanId = resolveTaiKhoanId(authentication);
		Set<String> roles = resolveRoles(authentication);
		phieuCuuTroService.capNhatDaXemTinNhan(phieuCuuTroId, request, taiKhoanId, roles);
	}

	@MessageMapping("/phieu-cuu-tro/{id}/tin-nhan/dang-go")
	public void capNhatTrangThaiDangGo(
			@DestinationVariable("id") Long phieuCuuTroId,
			CapNhatTrangThaiDangGoTinNhanRequest request,
			Principal principal) {
		Authentication authentication = resolveAuthentication(principal);
		Long taiKhoanId = resolveTaiKhoanId(authentication);
		Set<String> roles = resolveRoles(authentication);
		phieuCuuTroService.capNhatTrangThaiDangGoTinNhan(phieuCuuTroId, request, taiKhoanId, roles);
	}

	private Authentication resolveAuthentication(Principal principal) {
		if (!(principal instanceof Authentication authentication)) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized");
		}
		return authentication;
	}

	private Long resolveTaiKhoanId(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof Jwt jwt)) {
			throw new AuthenticationCredentialsNotFoundException("Unauthorized");
		}
		Object claim = jwt.getClaim("taiKhoanId");
		if (claim instanceof Number number) {
			return number.longValue();
		}
		if (claim instanceof String text && StringUtils.hasText(text)) {
			return Long.parseLong(text);
		}
		throw new AuthenticationCredentialsNotFoundException("Invalid token");
	}

	private Set<String> resolveRoles(Authentication authentication) {
		return authentication.getAuthorities()
				.stream()
				.map(grantedAuthority -> grantedAuthority.getAuthority())
				.filter(StringUtils::hasText)
				.collect(Collectors.toSet());
	}
}
