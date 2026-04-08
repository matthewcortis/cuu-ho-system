package com.backend.cuutro.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.TrangThaiDuyetTinhNguyenVien;
import com.backend.cuutro.dto.request.GanDoiNhomTinhNguyenVienRequest;
import com.backend.cuutro.dto.request.TinhNguyenVienDangKyRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomTinhNguyenVienDto;
import com.backend.cuutro.dto.response.entities.TinhNguyenVienDto;
import com.backend.cuutro.entities.DoiNhomEntity;
import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TinhNguyenVienEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.DoiNhomTinhNguyenVienMapper;
import com.backend.cuutro.mapper.TinhNguyenVienMapper;
import com.backend.cuutro.repository.DoiNhomRepository;
import com.backend.cuutro.repository.DoiNhomTinhNguyenVienRepository;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.TinhNguyenVienRepository;
import com.backend.cuutro.services.TinhNguyenVienService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TinhNguyenVienServiceImpl implements TinhNguyenVienService {

	private static final Set<String> VAI_TRO_HOP_LE = Set.of("truong_nhom", "pho_nhom", "thanh_vien");

	private final TinhNguyenVienRepository tinhNguyenVienRepository;
	private final NguoiDungRepository nguoiDungRepository;
	private final DoiNhomRepository doiNhomRepository;
	private final DoiNhomTinhNguyenVienRepository doiNhomTinhNguyenVienRepository;
	private final TinhNguyenVienMapper tinhNguyenVienMapper;
	private final DoiNhomTinhNguyenVienMapper doiNhomTinhNguyenVienMapper;

	@Override
	@Transactional
	public TinhNguyenVienDto dangKy(TinhNguyenVienDangKyRequest request) {
		if (tinhNguyenVienRepository.existsByNguoiDung_IdAndTrangThaiDuyetIn(
				request.getNguoiDungId(),
				List.of(
						TrangThaiDuyetTinhNguyenVien.CHO_XET_DUYET.name(),
						TrangThaiDuyetTinhNguyenVien.DUOC_DUYET.name()))) {
			throw new InvalidFieldException("Nguoi dung da co don tinh nguyen vien dang cho hoac da duoc duyet");
		}

		NguoiDungEntity nguoiDung = getNguoiDungOrThrow(request.getNguoiDungId());

		TinhNguyenVienEntity entity = new TinhNguyenVienEntity();
		entity.setNguoiDung(nguoiDung);
		entity.setThoiGian(request.getThoiGian());
		entity.setGhiChu(trimToNull(request.getGhiChu()));
		entity.setCoTheGiup(trimToNull(request.getCoTheGiup()));
		entity.setTrangThaiDuyet(TrangThaiDuyetTinhNguyenVien.CHO_XET_DUYET.name());
		entity.setThoiGianDuyet(null);

		return tinhNguyenVienMapper.toDto(tinhNguyenVienRepository.save(entity));
	}

	@Override
	public List<TinhNguyenVienDto> getDanhSach(String trangThaiDuyet) {
		List<TinhNguyenVienEntity> entities;
		if (!StringUtils.hasText(trangThaiDuyet)) {
			entities = tinhNguyenVienRepository.findAllByOrderByCreatedAtDesc();
		} else {
			TrangThaiDuyetTinhNguyenVien trangThai = parseTrangThaiDuyet(trangThaiDuyet);
			entities = tinhNguyenVienRepository.findByTrangThaiDuyetOrderByCreatedAtDesc(trangThai.name());
		}
		return tinhNguyenVienMapper.toDtoList(entities);
	}

	@Override
	public List<TinhNguyenVienDto> getChoXetDuyet() {
		return tinhNguyenVienMapper.toDtoList(
				tinhNguyenVienRepository.findByTrangThaiDuyetOrderByCreatedAtDesc(
						TrangThaiDuyetTinhNguyenVien.CHO_XET_DUYET.name()));
	}

	@Override
	public TinhNguyenVienDto getById(Long id) {
		return tinhNguyenVienMapper.toDto(getTinhNguyenVienOrThrow(id));
	}

	@Override
	@Transactional
	public TinhNguyenVienDto duyet(Long id) {
		TinhNguyenVienEntity entity = getTinhNguyenVienOrThrow(id);
		TrangThaiDuyetTinhNguyenVien trangThaiHienTai = parseTrangThaiDuyet(entity.getTrangThaiDuyet());
		if (trangThaiHienTai == TrangThaiDuyetTinhNguyenVien.HUY) {
			throw new InvalidFieldException("Khong the duyet tinh nguyen vien da huy");
		}
		if (trangThaiHienTai == TrangThaiDuyetTinhNguyenVien.DUOC_DUYET) {
			return tinhNguyenVienMapper.toDto(entity);
		}

		entity.setTrangThaiDuyet(TrangThaiDuyetTinhNguyenVien.DUOC_DUYET.name());
		entity.setThoiGianDuyet(LocalDateTime.now());
		return tinhNguyenVienMapper.toDto(tinhNguyenVienRepository.save(entity));
	}

	@Override
	@Transactional
	public void xoa(Long id) {
		TinhNguyenVienEntity entity = getTinhNguyenVienOrThrow(id);
		tinhNguyenVienRepository.delete(entity);
	}

	@Override
	@Transactional
	public DoiNhomTinhNguyenVienDto ganDoiNhom(GanDoiNhomTinhNguyenVienRequest request) {
		TinhNguyenVienEntity tinhNguyenVien = getTinhNguyenVienOrThrow(request.getTinhNguyenVienId());
		if (!TrangThaiDuyetTinhNguyenVien.DUOC_DUYET.name().equals(tinhNguyenVien.getTrangThaiDuyet())) {
			throw new InvalidFieldException("Chi duoc gan doi nhom cho tinh nguyen vien da duoc duyet");
		}

		if (doiNhomTinhNguyenVienRepository.countByTinhNguyenVien_Id(tinhNguyenVien.getId()) >= 3) {
			throw new InvalidFieldException("Mot tinh nguyen vien chi duoc tham gia toi da 3 doi nhom");
		}

		if (doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndTinhNguyenVien_Id(
				request.getDoiNhomId(), tinhNguyenVien.getId())) {
			throw new InvalidFieldException("Tinh nguyen vien da ton tai trong doi nhom nay");
		}

		DoiNhomEntity doiNhom = getDoiNhomOrThrow(request.getDoiNhomId());
		String vaiTro = chuanHoaVaiTro(request.getVaiTro());
		validateVaiTroDoiTruong(doiNhom.getId(), tinhNguyenVien.getId(), vaiTro);

		DoiNhomTinhNguyenVienEntity entity = new DoiNhomTinhNguyenVienEntity();
		entity.setDoiNhom(doiNhom);
		entity.setTinhNguyenVien(tinhNguyenVien);
		entity.setVaiTro(vaiTro);
		return doiNhomTinhNguyenVienMapper.toDto(doiNhomTinhNguyenVienRepository.save(entity));
	}

	private TinhNguyenVienEntity getTinhNguyenVienOrThrow(Long id) {
		return tinhNguyenVienRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("TinhNguyenVien not found with id=" + id));
	}

	private NguoiDungEntity getNguoiDungOrThrow(java.util.UUID id) {
		return nguoiDungRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("NguoiDung not found with id=" + id));
	}

	private DoiNhomEntity getDoiNhomOrThrow(Long id) {
		return doiNhomRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("DoiNhom not found with id=" + id));
	}

	private TrangThaiDuyetTinhNguyenVien parseTrangThaiDuyet(String rawStatus) {
		try {
			return TrangThaiDuyetTinhNguyenVien.valueOf(rawStatus.trim().toUpperCase());
		} catch (Exception ex) {
			throw new InvalidFieldException("trangThaiDuyet must be one of: CHO_XET_DUYET, DUOC_DUYET, HUY");
		}
	}

	private String chuanHoaVaiTro(String vaiTro) {
		if (!StringUtils.hasText(vaiTro)) {
			return "thanh_vien";
		}
		String normalized = vaiTro.trim().toLowerCase();
		if (!VAI_TRO_HOP_LE.contains(normalized)) {
			throw new InvalidFieldException("vaiTro must be one of: truong_nhom, pho_nhom, thanh_vien");
		}
		return normalized;
	}

	private void validateVaiTroDoiTruong(Long doiNhomId, Long tinhNguyenVienId, String vaiTro) {
		if (!"truong_nhom".equals(vaiTro)) {
			return;
		}
		if (doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndVaiTro(doiNhomId, "truong_nhom")) {
			throw new InvalidFieldException("Moi doi chi co 1 doi truong");
		}
		if (doiNhomTinhNguyenVienRepository.existsByTinhNguyenVien_IdAndVaiTro(tinhNguyenVienId, "truong_nhom")) {
			throw new InvalidFieldException("Tinh nguyen vien da la doi truong cua doi khac");
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? null : normalized;
	}
}
