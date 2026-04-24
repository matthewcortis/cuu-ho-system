package com.backend.cuutro.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.NguoiDungCapNhatTrangThaiTaiKhoanRequest;
import com.backend.cuutro.dto.request.NguoiDungAvatarUploadRequest;
import com.backend.cuutro.dto.request.NguoiDungDoiMatKhauRequest;
import com.backend.cuutro.dto.request.NguoiDungUpsertRequest;
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

	@PostMapping
	public ResponseEntity<ResponseData<NguoiDungDto>> create(
			@Valid @RequestBody NguoiDungUpsertRequest request,
			HttpServletRequest httpRequest) {
		NguoiDungDto created = nguoiDungService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Created nguoi dung successfully", created, httpRequest));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData<NguoiDungDto>> update(
			@PathVariable UUID id,
			@Valid @RequestBody NguoiDungUpsertRequest request,
			HttpServletRequest httpRequest) {
		NguoiDungDto updated = nguoiDungService.update(id, request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Updated nguoi dung successfully", updated, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> delete(
			@PathVariable UUID id,
			HttpServletRequest httpRequest) {
		nguoiDungService.delete(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Deleted nguoi dung successfully", null, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<NguoiDungDto>> getById(
			@PathVariable UUID id,
			HttpServletRequest httpRequest) {
		NguoiDungDto data = nguoiDungService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched nguoi dung successfully", data, httpRequest));
	}

	@GetMapping("/me")
	public ResponseEntity<ResponseData<NguoiDungDto>> getThongTinHienTai(HttpServletRequest httpRequest) {
		NguoiDungDto data = nguoiDungService.getThongTinHienTai();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched current user successfully", data, httpRequest));
	}

	@PutMapping("/me")
	public ResponseEntity<ResponseData<NguoiDungDto>> capNhatThongTinHienTai(
			@Valid @RequestBody NguoiDungUpsertRequest request,
			HttpServletRequest httpRequest) {
		NguoiDungDto updated = nguoiDungService.capNhatThongTinHienTai(request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Updated current user successfully", updated, httpRequest));
	}

	@PutMapping("/me/doi-mat-khau")
	public ResponseEntity<ResponseData<Void>> doiMatKhauHienTai(
			@Valid @RequestBody NguoiDungDoiMatKhauRequest request,
			HttpServletRequest httpRequest) {
		nguoiDungService.doiMatKhauHienTai(request.getMatKhauHienTai(), request.getMatKhauMoi());
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Changed current user password successfully", null, httpRequest));
	}

	@DeleteMapping("/me")
	public ResponseEntity<ResponseData<Void>> xoaThongTinHienTai(HttpServletRequest httpRequest) {
		nguoiDungService.xoaThongTinHienTai();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Deleted current user successfully", null, httpRequest));
	}

	@PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseData<NguoiDungDto>> uploadAvatarHienTai(
			@Valid @ModelAttribute NguoiDungAvatarUploadRequest request,
			HttpServletRequest httpRequest) {
		NguoiDungDto updated = nguoiDungService.capNhatAvatarHienTai(request.getAvatar());
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Uploaded current user avatar successfully", updated, httpRequest));
	}

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
