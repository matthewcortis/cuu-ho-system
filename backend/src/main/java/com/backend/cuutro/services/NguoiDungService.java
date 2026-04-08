package com.backend.cuutro.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.cuutro.dto.request.NguoiDungFilterRequest;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.specification.NguoiDungSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NguoiDungService {

	private final NguoiDungRepository nguoiDungRepository;

	public Page<NguoiDungEntity> search(NguoiDungFilterRequest filter, Pageable pageable) {
		return nguoiDungRepository.findAll(NguoiDungSpecifications.withFilter(filter), pageable);
	}

	public List<NguoiDungEntity> search(NguoiDungFilterRequest filter) {
		return nguoiDungRepository.findAll(NguoiDungSpecifications.withFilter(filter));
	}
}
