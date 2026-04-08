package com.backend.cuutro.services.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.cuutro.dto.request.DonViUpsertRequest;
import com.backend.cuutro.dto.response.entities.DonViDto;
import com.backend.cuutro.entities.DonViEntity;
import com.backend.cuutro.mapper.DonViMapper;
import com.backend.cuutro.repository.DonViRepository;
import com.backend.cuutro.services.DonViService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonViServiceImpl implements DonViService {

	private final DonViRepository donViRepository;
	private final DonViMapper donViMapper;

	@Override
	@Transactional
	public DonViDto create(DonViUpsertRequest request) {
		DonViEntity entity = new DonViEntity();
		applyRequest(entity, request);
		return donViMapper.toDto(donViRepository.save(entity));
	}

	@Override
	@Transactional
	public DonViDto update(Long id, DonViUpsertRequest request) {
		DonViEntity entity = getEntityOrThrow(id);
		applyRequest(entity, request);
		return donViMapper.toDto(donViRepository.save(entity));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		DonViEntity entity = getEntityOrThrow(id);
		donViRepository.delete(entity);
	}

	@Override
	public DonViDto getById(Long id) {
		return donViMapper.toDto(getEntityOrThrow(id));
	}

	@Override
	public List<DonViDto> getAll() {
		return donViMapper.toDtoList(donViRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
	}

	private DonViEntity getEntityOrThrow(Long id) {
		return donViRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("DonVi not found with id=" + id));
	}

	private void applyRequest(DonViEntity entity, DonViUpsertRequest request) {
		entity.setTen(request.getTen().trim());
		entity.setMaDonVi(request.getMaDonVi().trim());
	}
}

