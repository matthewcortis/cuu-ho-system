package com.backend.cuutro.services;

import java.util.List;

import com.backend.cuutro.dto.request.NhomVatPhamUpsertRequest;
import com.backend.cuutro.dto.response.entities.NhomVatPhamDto;

public interface NhomVatPhamService {

	NhomVatPhamDto create(NhomVatPhamUpsertRequest request);

	NhomVatPhamDto update(Long id, NhomVatPhamUpsertRequest request);

	void delete(Long id);

	NhomVatPhamDto getById(Long id);

	List<NhomVatPhamDto> getAll();
}

