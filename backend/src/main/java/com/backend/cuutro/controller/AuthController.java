package com.backend.cuutro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.DangNhapRequest;
import com.backend.cuutro.dto.request.DangKyRequest;
import com.backend.cuutro.dto.request.QuenMatKhauRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.DangNhapResponse;
import com.backend.cuutro.dto.response.entities.DangKyResponse;
import com.backend.cuutro.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<ResponseData<DangNhapResponse>> dangNhap(
			@Valid @RequestBody DangNhapRequest request,
			HttpServletRequest httpRequest) {
		DangNhapResponse data = authService.dangNhap(request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Dang nhap thanh cong", data, httpRequest));
	}

	@PostMapping("/register")
	public ResponseEntity<ResponseData<DangKyResponse>> dangKy(
			@Valid @RequestBody DangKyRequest request,
			HttpServletRequest httpRequest) {
		DangKyResponse data = authService.dangKy(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Dang ky thanh cong", data, httpRequest));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ResponseData<Void>> quenMatKhau(
			@Valid @RequestBody QuenMatKhauRequest request,
			HttpServletRequest httpRequest) {
		authService.quenMatKhau(request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Dat lai mat khau thanh cong", null, httpRequest));
	}

	private <T> ResponseData<T> buildResponse(HttpStatus status, String message, T data, HttpServletRequest request) {
		return ResponseData.<T>builder()
				.status(status.value())
				.message(message)
				.error(null)
				.data(data)
				.path(request.getRequestURI())
				.build();
	}
}
