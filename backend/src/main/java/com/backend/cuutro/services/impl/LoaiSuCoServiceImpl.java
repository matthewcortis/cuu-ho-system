package com.backend.cuutro.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.dto.request.BaseFilterRequest;
import com.backend.cuutro.dto.request.LoaiSuCoUpsertRequest;
import com.backend.cuutro.dto.response.entities.LoaiSuCoDto;
import com.backend.cuutro.entities.LoaiSuCoEntity;
import com.backend.cuutro.mapper.LoaiSuCoMapper;
import com.backend.cuutro.repository.LoaiSuCoRepository;
import com.backend.cuutro.repository.specification.LoaiSuCoSpecifications;
import com.backend.cuutro.services.LoaiSuCoService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoaiSuCoServiceImpl implements LoaiSuCoService {

	private final LoaiSuCoRepository loaiSuCoRepository;
	private final LoaiSuCoMapper loaiSuCoMapper;

	@Override
	@Transactional
	public LoaiSuCoDto create(LoaiSuCoUpsertRequest request) {
		LoaiSuCoEntity entity = new LoaiSuCoEntity();
		applyRequest(entity, request);
		return loaiSuCoMapper.toDto(loaiSuCoRepository.save(entity));
	}

	@Override
	@Transactional
	public LoaiSuCoDto update(Long id, LoaiSuCoUpsertRequest request) {
		LoaiSuCoEntity entity = getEntityOrThrow(id);
		applyRequest(entity, request);
		return loaiSuCoMapper.toDto(loaiSuCoRepository.save(entity));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		LoaiSuCoEntity entity = getEntityOrThrow(id);
		loaiSuCoRepository.delete(entity);
	}

	@Override
	public LoaiSuCoDto getById(Long id) {
		return loaiSuCoMapper.toDto(getEntityOrThrow(id));
	}

	@Override
	public List<LoaiSuCoDto> getAll() {
		return loaiSuCoMapper.toDtoList(loaiSuCoRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
	}

	@Override
	public Page<LoaiSuCoDto> filter(BaseFilterRequest filterRequest) {
		int page = filterRequest == null || filterRequest.getPage() == null || filterRequest.getPage() < 0
				? 0
				: filterRequest.getPage();
		int size = filterRequest == null || filterRequest.getSize() == null || filterRequest.getSize() <= 0
				? 20
				: filterRequest.getSize();
		Pageable pageable = PageRequest.of(page, size, LoaiSuCoSpecifications.toSort(filterRequest));

		return loaiSuCoMapper.toDtoPage(
				loaiSuCoRepository.findAll(LoaiSuCoSpecifications.withFilter(filterRequest), pageable));
	}

	private LoaiSuCoEntity getEntityOrThrow(Long id) {
		return loaiSuCoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("LoaiSuCo not found with id=" + id));
	}

	private void applyRequest(LoaiSuCoEntity entity, LoaiSuCoUpsertRequest request) {
		entity.setTen(request.getTen().trim());
		entity.setIconUrl(normalizeNullable(request.getIconUrl()));
		entity.setMoTa(normalizeNullable(request.getMoTa()));
		if (request.getTrangThai() != null) {
			entity.setTrangThai(request.getTrangThai());
		} else if (entity.getTrangThai() == null) {
			entity.setTrangThai(Boolean.TRUE);
		}
	}

	private String normalizeNullable(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}
}
