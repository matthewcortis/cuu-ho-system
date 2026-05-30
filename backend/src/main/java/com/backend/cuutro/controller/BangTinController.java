package com.backend.cuutro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.BangTinCreateRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.BangTinDto;
import com.backend.cuutro.services.BangTinService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bang-tin")
@RequiredArgsConstructor
public class BangTinController {

	private final BangTinService bangTinService;

	@PostMapping
	public ResponseEntity<ResponseData<BangTinDto>> create(
			@Valid @RequestBody BangTinCreateRequest request,
			HttpServletRequest httpRequest) {
		BangTinDto created = bangTinService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Tao bai bang tin thanh cong", created, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<BangTinDto>>> getDanhSachCongKhai(HttpServletRequest httpRequest) {
		List<BangTinDto> data = bangTinService.getDanhSachCongKhai();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched bang tin list successfully", data, httpRequest));
	}

	@GetMapping("/quan-ly")
	public ResponseEntity<ResponseData<List<BangTinDto>>> getDanhSachQuanLy(HttpServletRequest httpRequest) {
		List<BangTinDto> data = bangTinService.getDanhSachQuanLy();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched admin bang tin list successfully", data, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<BangTinDto>> getByIdCongKhai(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		BangTinDto data = bangTinService.getByIdCongKhai(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched bang tin successfully", data, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> xoaBangTin(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		bangTinService.xoaBangTin(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Xoa bang tin thanh cong", null, httpRequest));
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
