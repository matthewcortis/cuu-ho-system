package com.backend.cuutro.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.backend.cuutro.dto.request.BaseFilterRequest;
import com.backend.cuutro.dto.request.LoaiSuCoUpsertRequest;
import com.backend.cuutro.dto.response.entities.LoaiSuCoDto;

public interface LoaiSuCoService {

	LoaiSuCoDto create(LoaiSuCoUpsertRequest request);

	LoaiSuCoDto update(Long id, LoaiSuCoUpsertRequest request);

	void delete(Long id);

	LoaiSuCoDto getById(Long id);

	List<LoaiSuCoDto> getAll();

	Page<LoaiSuCoDto> filter(BaseFilterRequest filterRequest);
}
