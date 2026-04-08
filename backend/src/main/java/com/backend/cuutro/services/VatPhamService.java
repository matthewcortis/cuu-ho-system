package com.backend.cuutro.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.cuutro.dto.request.VatPhamCreateWithImageRequest;
import com.backend.cuutro.dto.request.VatPhamFilterRequest;
import com.backend.cuutro.dto.request.VatPhamUpsertRequest;
import com.backend.cuutro.dto.response.entities.VatPhamDto;
import com.backend.cuutro.entities.VatPhamEntity;

public interface VatPhamService {

	Page<VatPhamEntity> search(VatPhamFilterRequest filter, Pageable pageable);

	List<VatPhamEntity> search(VatPhamFilterRequest filter);

	VatPhamDto create(VatPhamUpsertRequest request);

	VatPhamDto createWithImage(VatPhamCreateWithImageRequest request);

	VatPhamDto update(Long id, VatPhamUpsertRequest request);

	void delete(Long id);

	VatPhamDto getById(Long id);

	List<VatPhamDto> getAll();
}
