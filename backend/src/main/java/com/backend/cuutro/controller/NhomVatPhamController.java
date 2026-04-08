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

import com.backend.cuutro.dto.request.NhomVatPhamUpsertRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.NhomVatPhamDto;
import com.backend.cuutro.services.NhomVatPhamService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/nhom-vat-pham")
@RequiredArgsConstructor
public class NhomVatPhamController {

	private final NhomVatPhamService nhomVatPhamService;

	@PostMapping
	public ResponseEntity<ResponseData<NhomVatPhamDto>> create(
			@Valid @RequestBody NhomVatPhamUpsertRequest request,
			HttpServletRequest httpRequest) {
		NhomVatPhamDto created = nhomVatPhamService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Created nhom vat pham successfully", created, httpRequest));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData<NhomVatPhamDto>> update(
			@PathVariable Long id,
			@Valid @RequestBody NhomVatPhamUpsertRequest request,
			HttpServletRequest httpRequest) {
		NhomVatPhamDto updated = nhomVatPhamService.update(id, request);
		return ResponseEntity.ok(
				buildResponse(HttpStatus.OK, "Updated nhom vat pham successfully", updated, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> delete(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		nhomVatPhamService.delete(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Deleted nhom vat pham successfully", null, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<NhomVatPhamDto>> getById(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		NhomVatPhamDto data = nhomVatPhamService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched nhom vat pham successfully", data, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<NhomVatPhamDto>>> getAll(HttpServletRequest httpRequest) {
		List<NhomVatPhamDto> data = nhomVatPhamService.getAll();
		return ResponseEntity.ok(
				buildResponse(HttpStatus.OK, "Fetched nhom vat pham list successfully", data, httpRequest));
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

