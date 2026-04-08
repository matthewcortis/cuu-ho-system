package com.backend.cuutro.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.cuutro.constant.enums.TrangThaiDuyetTinhNguyenVien;
import com.backend.cuutro.dto.request.DoiNhomTaoRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomDto;
import com.backend.cuutro.entities.DoiNhomEntity;
import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;
import com.backend.cuutro.entities.TinhNguyenVienEntity;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.DoiNhomMapper;
import com.backend.cuutro.repository.DoiNhomRepository;
import com.backend.cuutro.repository.DoiNhomTinhNguyenVienRepository;
import com.backend.cuutro.repository.TinhNguyenVienRepository;
import com.backend.cuutro.repository.ViTriRepository;
import com.backend.cuutro.services.DoiNhomService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoiNhomServiceImpl implements DoiNhomService {

	private final DoiNhomRepository doiNhomRepository;
	private final DoiNhomTinhNguyenVienRepository doiNhomTinhNguyenVienRepository;
	private final TinhNguyenVienRepository tinhNguyenVienRepository;
	private final ViTriRepository viTriRepository;
	private final DoiNhomMapper doiNhomMapper;

	@Override
	@Transactional
	public DoiNhomDto taoDoiNhom(DoiNhomTaoRequest request) {
		TinhNguyenVienEntity doiTruong = getTinhNguyenVienOrThrow(request.getDoiTruongTinhNguyenVienId());
		if (!TrangThaiDuyetTinhNguyenVien.DUOC_DUYET.name().equals(doiTruong.getTrangThaiDuyet())) {
			throw new InvalidFieldException("Chi tinh nguyen vien DUOC_DUYET moi duoc lam doi truong");
		}
		if (doiNhomTinhNguyenVienRepository.existsByTinhNguyenVien_IdAndVaiTro(doiTruong.getId(), "truong_nhom")) {
			throw new InvalidFieldException("Nguoi duoc chon da la doi truong cua doi khac");
		}
		if (doiNhomTinhNguyenVienRepository.countByTinhNguyenVien_Id(doiTruong.getId()) >= 3) {
			throw new InvalidFieldException("Mot tinh nguyen vien chi duoc tham gia toi da 3 doi nhom");
		}

		ViTriEntity viTri = getViTriOrThrow(request.getViTriId());

		DoiNhomEntity doiNhom = new DoiNhomEntity();
		doiNhom.setTenDoiNhom(request.getTenDoiNhom().trim());
		doiNhom.setSoDienThoai(request.getSoDienThoai().trim());
		doiNhom.setViTri(viTri);
		doiNhom.setTrangThaiHoatDong(Boolean.TRUE);
		doiNhom.setActive(Boolean.TRUE);
		doiNhom.setTrangThai("idle");
		DoiNhomEntity savedDoiNhom = doiNhomRepository.save(doiNhom);

		DoiNhomTinhNguyenVienEntity phanCongVaiTro = new DoiNhomTinhNguyenVienEntity();
		phanCongVaiTro.setDoiNhom(savedDoiNhom);
		phanCongVaiTro.setTinhNguyenVien(doiTruong);
		phanCongVaiTro.setVaiTro("truong_nhom");
		doiNhomTinhNguyenVienRepository.save(phanCongVaiTro);

		return doiNhomMapper.toDto(savedDoiNhom);
	}

	private TinhNguyenVienEntity getTinhNguyenVienOrThrow(Long id) {
		return tinhNguyenVienRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("TinhNguyenVien not found with id=" + id));
	}

	private ViTriEntity getViTriOrThrow(Long id) {
		return viTriRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("ViTri not found with id=" + id));
	}
}
