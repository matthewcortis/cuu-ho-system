package com.backend.cuutro.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.cuutro.dto.request.DoiNhomTaoRequest;
import com.backend.cuutro.dto.response.ResponseData;
import com.backend.cuutro.dto.response.entities.DoiNhomDto;
import com.backend.cuutro.services.DoiNhomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/doi-nhom")
@RequiredArgsConstructor
public class DoiNhomController {

	private final DoiNhomService doiNhomService;

	@PostMapping
	public ResponseEntity<ResponseData<DoiNhomDto>> taoDoiNhom(
			@Valid @RequestBody DoiNhomTaoRequest request,
			HttpServletRequest httpRequest) {
		DoiNhomDto created = doiNhomService.taoDoiNhom(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(buildResponse(HttpStatus.CREATED, "Tao doi nhom thanh cong", created, httpRequest));
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

