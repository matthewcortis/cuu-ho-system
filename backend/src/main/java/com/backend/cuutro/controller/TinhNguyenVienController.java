package com.backend.cuutro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.GanDoiNhomTinhNguyenVienRequest;
import com.backend.cuutro.dto.request.TinhNguyenVienDangKyRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.DoiNhomTinhNguyenVienDto;
import com.backend.cuutro.dto.response.entities.TinhNguyenVienDto;
import com.backend.cuutro.services.TinhNguyenVienService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tinh-nguyen-vien")
@RequiredArgsConstructor
public class TinhNguyenVienController {

	private final TinhNguyenVienService tinhNguyenVienService;

	@PostMapping("/dang-ky")
	public ResponseEntity<ResponseData<TinhNguyenVienDto>> dangKy(
			@Valid @RequestBody TinhNguyenVienDangKyRequest request,
			HttpServletRequest httpRequest) {
		TinhNguyenVienDto created = tinhNguyenVienService.dangKy(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Dang ky tinh nguyen vien thanh cong", created, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<TinhNguyenVienDto>>> getDanhSach(
			@RequestParam(required = false) String trangThaiDuyet,
			HttpServletRequest httpRequest) {
		List<TinhNguyenVienDto> data = tinhNguyenVienService.getDanhSach(trangThaiDuyet);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched tinh nguyen vien list successfully", data, httpRequest));
	}

	@GetMapping("/cho-xet-duyet")
	public ResponseEntity<ResponseData<List<TinhNguyenVienDto>>> getChoXetDuyet(HttpServletRequest httpRequest) {
		List<TinhNguyenVienDto> data = tinhNguyenVienService.getChoXetDuyet();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched cho xet duyet list successfully", data, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<TinhNguyenVienDto>> getById(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		TinhNguyenVienDto data = tinhNguyenVienService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched tinh nguyen vien successfully", data, httpRequest));
	}

	@PutMapping("/{id}/duyet")
	public ResponseEntity<ResponseData<TinhNguyenVienDto>> duyet(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		TinhNguyenVienDto updated = tinhNguyenVienService.duyet(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Duyet tinh nguyen vien thanh cong", updated, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> xoa(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		tinhNguyenVienService.xoa(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Xoa tinh nguyen vien thanh cong", null, httpRequest));
	}

	@PostMapping("/gan-doi-nhom")
	public ResponseEntity<ResponseData<DoiNhomTinhNguyenVienDto>> ganDoiNhom(
			@Valid @RequestBody GanDoiNhomTinhNguyenVienRequest request,
			HttpServletRequest httpRequest) {
		DoiNhomTinhNguyenVienDto created = tinhNguyenVienService.ganDoiNhom(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Gan tinh nguyen vien vao doi nhom thanh cong", created, httpRequest));
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

