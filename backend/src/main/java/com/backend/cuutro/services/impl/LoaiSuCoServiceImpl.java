package com.backend.cuutro.services.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.cuutro.dto.request.LoaiSuCoUpsertRequest;
import com.backend.cuutro.dto.response.entities.LoaiSuCoDto;
import com.backend.cuutro.entities.LoaiSuCoEntity;
import com.backend.cuutro.mapper.LoaiSuCoMapper;
import com.backend.cuutro.repository.LoaiSuCoRepository;
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

	private LoaiSuCoEntity getEntityOrThrow(Long id) {
		return loaiSuCoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("LoaiSuCo not found with id=" + id));
	}

	private void applyRequest(LoaiSuCoEntity entity, LoaiSuCoUpsertRequest request) {
		entity.setTen(request.getTen().trim());
		if (request.getIconUrl() == null) {
			entity.setIconUrl(null);
			return;
		}
		String iconUrl = request.getIconUrl().trim();
		entity.setIconUrl(iconUrl.isEmpty() ? null : iconUrl);
	}
}

