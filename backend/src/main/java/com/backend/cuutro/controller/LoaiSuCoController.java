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
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.LoaiSuCoUpsertRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.LoaiSuCoDto;
import com.backend.cuutro.services.LoaiSuCoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/loai-su-co")
@RequiredArgsConstructor
public class LoaiSuCoController {

	private final LoaiSuCoService loaiSuCoService;

	@PostMapping
	public ResponseEntity<ResponseData<LoaiSuCoDto>> create(
			@Valid @RequestBody LoaiSuCoUpsertRequest request,
			HttpServletRequest httpRequest) {
		LoaiSuCoDto created = loaiSuCoService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Created loai su co successfully", created, httpRequest));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData<LoaiSuCoDto>> update(
			@PathVariable Long id,
			@Valid @RequestBody LoaiSuCoUpsertRequest request,
			HttpServletRequest httpRequest) {
		LoaiSuCoDto updated = loaiSuCoService.update(id, request);
		return ResponseEntity.ok(
				buildResponse(HttpStatus.OK, "Updated loai su co successfully", updated, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> delete(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		loaiSuCoService.delete(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Deleted loai su co successfully", null, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<LoaiSuCoDto>> getById(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		LoaiSuCoDto data = loaiSuCoService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched loai su co successfully", data, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<LoaiSuCoDto>>> getAll(HttpServletRequest httpRequest) {
		List<LoaiSuCoDto> data = loaiSuCoService.getAll();
		return ResponseEntity.ok(
				buildResponse(HttpStatus.OK, "Fetched loai su co list successfully", data, httpRequest));
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

