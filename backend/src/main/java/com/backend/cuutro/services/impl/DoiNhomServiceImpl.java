package com.backend.cuutro.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.TrangThaiDuyetTinhNguyenVien;
import com.backend.cuutro.dto.request.DoiNhomTaoRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomDto;
import com.backend.cuutro.dto.response.entities.DoiNhomThanhVienDto;
import com.backend.cuutro.entities.DoiNhomEntity;
import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TinhNguyenVienEntity;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.DoiNhomMapper;
import com.backend.cuutro.repository.DoiNhomRepository;
import com.backend.cuutro.repository.DoiNhomTinhNguyenVienRepository;
import com.backend.cuutro.repository.TinhNguyenVienRepository;
import com.backend.cuutro.services.DoiNhomService;
import com.backend.cuutro.services.ViTriCommandService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoiNhomServiceImpl implements DoiNhomService {

	private final DoiNhomRepository doiNhomRepository;
	private final DoiNhomTinhNguyenVienRepository doiNhomTinhNguyenVienRepository;
	private final TinhNguyenVienRepository tinhNguyenVienRepository;
	private final ViTriCommandService viTriCommandService;
	private final DoiNhomMapper doiNhomMapper;

	@Override
	public List<DoiNhomDto> getDanhSach() {
		List<DoiNhomEntity> doiNhoms = doiNhomRepository.findAllByOrderByCreatedAtDesc();
		List<DoiNhomDto> dtoList = doiNhomMapper.toDtoList(doiNhoms);
		if (dtoList.isEmpty()) {
			return dtoList;
		}

		List<Long> doiNhomIds = dtoList.stream()
				.map(DoiNhomDto::getId)
				.filter(Objects::nonNull)
				.toList();
		if (doiNhomIds.isEmpty()) {
			return dtoList;
		}

		List<DoiNhomTinhNguyenVienEntity> phanCongEntities = doiNhomTinhNguyenVienRepository
				.findByDoiNhom_IdInOrderByCreatedAtAsc(doiNhomIds);
		Map<Long, List<DoiNhomTinhNguyenVienEntity>> phanCongByDoiNhomId = phanCongEntities.stream()
				.filter(item -> item.getDoiNhom() != null && item.getDoiNhom().getId() != null)
				.collect(Collectors.groupingBy(item -> item.getDoiNhom().getId()));

		for (DoiNhomDto dto : dtoList) {
			List<DoiNhomThanhVienDto> thanhViens = phanCongByDoiNhomId
					.getOrDefault(dto.getId(), Collections.emptyList())
					.stream()
					.map(this::toThanhVienDto)
					.filter(Objects::nonNull)
					.toList();

			DoiNhomThanhVienDto doiTruong = thanhViens.stream()
					.filter(item -> "truong_nhom".equalsIgnoreCase(item.getVaiTro()))
					.findFirst()
					.orElse(null);

			dto.setThanhViens(thanhViens);
			dto.setSoLuongThanhVien(thanhViens.size());
			dto.setDoiTruong(doiTruong);
		}

		return dtoList;
	}

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

		ViTriEntity viTri = viTriCommandService.taoViTriMoi(request.getViTri());

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

		DoiNhomDto dto = doiNhomMapper.toDto(savedDoiNhom);
		DoiNhomThanhVienDto doiTruongDto = toThanhVienDto(phanCongVaiTro);
		dto.setDoiTruong(doiTruongDto);
		dto.setThanhViens(doiTruongDto == null ? List.of() : List.of(doiTruongDto));
		dto.setSoLuongThanhVien(doiTruongDto == null ? 0 : 1);
		return dto;
	}

	private TinhNguyenVienEntity getTinhNguyenVienOrThrow(Long id) {
		return tinhNguyenVienRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("TinhNguyenVien not found with id=" + id));
	}

	private DoiNhomThanhVienDto toThanhVienDto(DoiNhomTinhNguyenVienEntity phanCong) {
		if (phanCong == null || phanCong.getTinhNguyenVien() == null) {
			return null;
		}

		TinhNguyenVienEntity tinhNguyenVien = phanCong.getTinhNguyenVien();
		NguoiDungEntity nguoiDung = tinhNguyenVien.getNguoiDung();
		String defaultTen = "Tinh nguyen vien #" + tinhNguyenVien.getId();
		String ten = nguoiDung != null && StringUtils.hasText(nguoiDung.getTen())
				? nguoiDung.getTen().trim()
				: defaultTen;
		String sdt = nguoiDung != null && StringUtils.hasText(nguoiDung.getSdt())
				? nguoiDung.getSdt().trim()
				: "";
		String avatarUrl = nguoiDung != null && StringUtils.hasText(nguoiDung.getAvatarUrl())
				? nguoiDung.getAvatarUrl().trim()
				: "";
		String vaiTro = StringUtils.hasText(phanCong.getVaiTro()) ? phanCong.getVaiTro().trim().toLowerCase() : "thanh_vien";

		return DoiNhomThanhVienDto.builder()
				.tinhNguyenVienId(tinhNguyenVien.getId())
				.ten(ten)
				.sdt(sdt)
				.avatarUrl(avatarUrl)
				.vaiTro(vaiTro)
				.build();
	}
}
