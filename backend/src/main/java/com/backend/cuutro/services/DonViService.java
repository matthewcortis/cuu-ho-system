package com.backend.cuutro.services;

import java.util.List;

import com.backend.cuutro.dto.request.DonViUpsertRequest;
import com.backend.cuutro.dto.response.entities.DonViDto;

public interface DonViService {

	DonViDto create(DonViUpsertRequest request);

	DonViDto update(Long id, DonViUpsertRequest request);

	void delete(Long id);

	DonViDto getById(Long id);

	List<DonViDto> getAll();
}

