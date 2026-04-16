package com.backend.cuutro.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.cuutro.dto.request.NguoiDungFilterRequest;
import com.backend.cuutro.dto.response.entities.NguoiDungDto;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TaiKhoanEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.NguoiDungMapper;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.TaiKhoanRepository;
import com.backend.cuutro.repository.specification.NguoiDungSpecifications;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NguoiDungService {

	private final NguoiDungRepository nguoiDungRepository;
	private final TaiKhoanRepository taiKhoanRepository;
	private final NguoiDungMapper nguoiDungMapper;

	public Page<NguoiDungEntity> search(NguoiDungFilterRequest filter, Pageable pageable) {
		return nguoiDungRepository.findAll(NguoiDungSpecifications.withFilter(filter), pageable);
	}

	public List<NguoiDungEntity> search(NguoiDungFilterRequest filter) {
		return nguoiDungRepository.findAll(NguoiDungSpecifications.withFilter(filter));
	}

	public List<NguoiDungDto> getDanhSach() {
		List<NguoiDungEntity> entities = nguoiDungRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
		return nguoiDungMapper.toDtoList(entities);
	}

	@Transactional
	public NguoiDungDto capNhatTrangThaiTaiKhoan(UUID nguoiDungId, Boolean trangThai) {
		NguoiDungEntity nguoiDung = nguoiDungRepository.findById(nguoiDungId)
				.orElseThrow(() -> new EntityNotFoundException("NguoiDung not found with id=" + nguoiDungId));

		TaiKhoanEntity taiKhoan = nguoiDung.getTaiKhoan();
		if (taiKhoan == null || taiKhoan.getId() == null) {
			throw new InvalidFieldException("Nguoi dung chua co tai khoan de cap nhat trang thai");
		}

		taiKhoan.setTrangThai(Boolean.TRUE.equals(trangThai));
		taiKhoanRepository.save(taiKhoan);
		nguoiDung.setTaiKhoan(taiKhoan);

		return nguoiDungMapper.toDto(nguoiDung);
	}
}
