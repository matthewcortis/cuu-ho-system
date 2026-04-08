package com.backend.cuutro.controller;

import java.util.List;

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

import com.backend.cuutro.dto.request.VatPhamCreateWithImageRequest;
import com.backend.cuutro.dto.request.VatPhamUpsertRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.VatPhamDto;
import com.backend.cuutro.services.VatPhamService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vat-pham")
@RequiredArgsConstructor
public class VatPhamController {

	private final VatPhamService vatPhamService;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseData<VatPhamDto>> create(
			@Valid @RequestBody VatPhamUpsertRequest request,
			HttpServletRequest httpRequest) {
		VatPhamDto created = vatPhamService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Created vat pham successfully", created, httpRequest));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ResponseData<VatPhamDto>> createWithImage(
			@Valid @ModelAttribute VatPhamCreateWithImageRequest request,
			HttpServletRequest httpRequest) {
		VatPhamDto created = vatPhamService.createWithImage(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Created vat pham successfully", created, httpRequest));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData<VatPhamDto>> update(
			@PathVariable Long id,
			@Valid @RequestBody VatPhamUpsertRequest request,
			HttpServletRequest httpRequest) {
		VatPhamDto updated = vatPhamService.update(id, request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Updated vat pham successfully", updated, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> delete(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		vatPhamService.delete(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Deleted vat pham successfully", null, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<VatPhamDto>> getById(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		VatPhamDto data = vatPhamService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched vat pham successfully", data, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<VatPhamDto>>> getAll(HttpServletRequest httpRequest) {
		List<VatPhamDto> data = vatPhamService.getAll();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched vat pham list successfully", data, httpRequest));
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
