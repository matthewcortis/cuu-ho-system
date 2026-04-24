package com.backend.cuutro.services.impl;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.cuutro.dto.request.NhomVatPhamUpsertRequest;
import com.backend.cuutro.dto.response.entities.NhomVatPhamDto;
import com.backend.cuutro.entities.LoaiSuCoEntity;
import com.backend.cuutro.entities.NhomVatPhamEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.NhomVatPhamMapper;
import com.backend.cuutro.repository.LoaiSuCoRepository;
import com.backend.cuutro.repository.NhomVatPhamRepository;
import com.backend.cuutro.services.NhomVatPhamService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NhomVatPhamServiceImpl implements NhomVatPhamService {

	private final NhomVatPhamRepository nhomVatPhamRepository;
	private final LoaiSuCoRepository loaiSuCoRepository;
	private final NhomVatPhamMapper nhomVatPhamMapper;

	@Override
	@Transactional
	public NhomVatPhamDto create(NhomVatPhamUpsertRequest request) {
		NhomVatPhamEntity entity = new NhomVatPhamEntity();
		applyRequest(entity, request);
		return nhomVatPhamMapper.toDto(nhomVatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public NhomVatPhamDto update(Long id, NhomVatPhamUpsertRequest request) {
		NhomVatPhamEntity entity = getEntityOrThrow(id);
		applyRequest(entity, request);
		return nhomVatPhamMapper.toDto(nhomVatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		NhomVatPhamEntity entity = getEntityOrThrow(id);
		nhomVatPhamRepository.delete(entity);
	}

	@Override
	public NhomVatPhamDto getById(Long id) {
		return nhomVatPhamMapper.toDto(getEntityOrThrow(id));
	}

	@Override
	public List<NhomVatPhamDto> getAll() {
		return nhomVatPhamMapper.toDtoList(nhomVatPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
	}

	private NhomVatPhamEntity getEntityOrThrow(Long id) {
		return nhomVatPhamRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("NhomVatPham not found with id=" + id));
	}

	private void applyRequest(NhomVatPhamEntity entity, NhomVatPhamUpsertRequest request) {
		entity.setTen(request.getTen().trim());
		entity.setMoTa(request.getMoTa() == null ? null : request.getMoTa().trim());
		entity.setLoaiSuCos(resolveLoaiSuCos(request));
	}

	private Set<LoaiSuCoEntity> resolveLoaiSuCos(NhomVatPhamUpsertRequest request) {
		LinkedHashSet<Long> loaiSuCoIds = new LinkedHashSet<>();
		if (request.getLoaiSuCoIds() != null) {
			for (Long loaiSuCoId : request.getLoaiSuCoIds()) {
				if (loaiSuCoId != null) {
					loaiSuCoIds.add(loaiSuCoId);
				}
			}
		}
		if (request.getLoaiSuCoId() != null) {
			loaiSuCoIds.add(request.getLoaiSuCoId());
		}
		if (loaiSuCoIds.isEmpty()) {
			throw new InvalidFieldException("loaiSuCoIds is required");
		}

		List<LoaiSuCoEntity> loaiSuCoEntities = loaiSuCoRepository.findAllById(loaiSuCoIds);
		Map<Long, LoaiSuCoEntity> loaiSuCoById = loaiSuCoEntities.stream()
				.collect(Collectors.toMap(LoaiSuCoEntity::getId, item -> item));

		List<Long> missingIds = loaiSuCoIds.stream()
				.filter(id -> !loaiSuCoById.containsKey(id))
				.toList();
		if (!missingIds.isEmpty()) {
			throw new EntityNotFoundException("LoaiSuCo not found with ids=" + missingIds);
		}

		LinkedHashSet<LoaiSuCoEntity> result = new LinkedHashSet<>();
		for (Long loaiSuCoId : loaiSuCoIds) {
			result.add(loaiSuCoById.get(loaiSuCoId));
		}
		return result;
	}
}
