package com.backend.cuutro.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.ViTriInputRequest;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.repository.ViTriRepository;
import com.backend.cuutro.services.ViTriCommandService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ViTriCommandServiceImpl implements ViTriCommandService {

	private final ViTriRepository viTriRepository;

	@Override
	@Transactional
	public ViTriEntity taoViTriMoi(ViTriInputRequest request) {
		if (request == null) {
			throw new InvalidFieldException("viTri is required");
		}

		ViTriEntity viTri = new ViTriEntity();
		viTri.setDiaChi(normalizeRequired(request.getDiaChi(), "viTri.diaChi is required"));
		viTri.setLat(normalizeNullable(request.getLat()));
		viTri.setLongitude(normalizeNullable(request.getLongitude()));
		return viTriRepository.save(viTri);
	}

	private String normalizeRequired(String value, String errorMessage) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(errorMessage);
		}
		return value.trim();
	}

	private String normalizeNullable(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
