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

import com.backend.cuutro.dto.request.DonViUpsertRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.DonViDto;
import com.backend.cuutro.services.DonViService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/don-vi")
@RequiredArgsConstructor
public class DonViController {

	private final DonViService donViService;

	@PostMapping
	public ResponseEntity<ResponseData<DonViDto>> create(
			@Valid @RequestBody DonViUpsertRequest request,
			HttpServletRequest httpRequest) {
		DonViDto created = donViService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Created don vi successfully", created, httpRequest));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ResponseData<DonViDto>> update(
			@PathVariable Long id,
			@Valid @RequestBody DonViUpsertRequest request,
			HttpServletRequest httpRequest) {
		DonViDto updated = donViService.update(id, request);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Updated don vi successfully", updated, httpRequest));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ResponseData<Void>> delete(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		donViService.delete(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Deleted don vi successfully", null, httpRequest));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ResponseData<DonViDto>> getById(
			@PathVariable Long id,
			HttpServletRequest httpRequest) {
		DonViDto data = donViService.getById(id);
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched don vi successfully", data, httpRequest));
	}

	@GetMapping
	public ResponseEntity<ResponseData<List<DonViDto>>> getAll(HttpServletRequest httpRequest) {
		List<DonViDto> data = donViService.getAll();
		return ResponseEntity.ok(buildResponse(HttpStatus.OK, "Fetched don vi list successfully", data, httpRequest));
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

