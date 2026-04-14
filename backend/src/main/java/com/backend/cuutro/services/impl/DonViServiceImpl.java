package com.backend.cuutro.services.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
	private final JdbcTemplate jdbcTemplate;

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
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<DonViDto> getAll() {
		try {
			return donViMapper.toDtoList(donViRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
		} catch (RuntimeException ex) {
			return getAllFallbackByJdbc();
		}
	}

	private DonViEntity getEntityOrThrow(Long id) {
		return donViRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("DonVi not found with id=" + id));
	}

	private void applyRequest(DonViEntity entity, DonViUpsertRequest request) {
		entity.setTen(request.getTen().trim());
		entity.setMaDonVi(request.getMaDonVi().trim());
	}

	private List<DonViDto> getAllFallbackByJdbc() {
		boolean hasMaDonVi = hasColumn("ma_don_vi");
		String sql = hasMaDonVi
				? "SELECT id, ten, ma_don_vi FROM don_vi ORDER BY id DESC"
				: "SELECT id, ten, NULL::varchar AS ma_don_vi FROM don_vi ORDER BY id DESC";

		return jdbcTemplate.query(sql, (rs, rowNum) -> DonViDto.builder()
				.id(rs.getLong("id"))
				.ten(rs.getString("ten"))
				.maDonVi(rs.getString("ma_don_vi"))
				.createdAt(null)
				.build());
	}

	private boolean hasColumn(String columnName) {
		Boolean exists = jdbcTemplate.queryForObject(
				"""
						SELECT EXISTS (
						  SELECT 1
						  FROM information_schema.columns
						  WHERE table_schema = 'public'
						    AND table_name = 'don_vi'
						    AND column_name = ?
						)
						""",
				Boolean.class,
				columnName);
		return Boolean.TRUE.equals(exists);
	}
}
