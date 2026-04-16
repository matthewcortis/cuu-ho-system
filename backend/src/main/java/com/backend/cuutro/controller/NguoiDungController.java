package com.backend.cuutro.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.NguoiDungCapNhatTrangThaiTaiKhoanRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.NguoiDungDto;
import com.backend.cuutro.services.NguoiDungService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/nguoi-dung")
@RequiredArgsConstructor
public class NguoiDungController {

	private final NguoiDungService nguoiDungService;

	@GetMapping
	public ResponseEntity<ResponseData<List<NguoiDungDto>>> getDanhSach(HttpServletRequest httpRequest) {
		List<NguoiDungDto> data = nguoiDungService.getDanhSach();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched nguoi dung list successfully", data, httpRequest));
	}

	@PutMapping("/{id}/trang-thai-tai-khoan")
	public ResponseEntity<ResponseData<NguoiDungDto>> capNhatTrangThaiTaiKhoan(
			@PathVariable UUID id,
			@Valid @RequestBody NguoiDungCapNhatTrangThaiTaiKhoanRequest request,
			HttpServletRequest httpRequest) {
		NguoiDungDto updated = nguoiDungService.capNhatTrangThaiTaiKhoan(id, request.getTrangThai());
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Cap nhat trang thai tai khoan thanh cong", updated, httpRequest));
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
