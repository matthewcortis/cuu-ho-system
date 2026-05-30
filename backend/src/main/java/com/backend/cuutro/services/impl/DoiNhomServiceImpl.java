package com.backend.cuutro.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.TrangThaiDuyetTinhNguyenVien;
import com.backend.cuutro.constant.enums.RoleType;
import com.backend.cuutro.dto.request.DoiNhomCapNhatRequest;
import com.backend.cuutro.dto.request.DoiNhomTaoRequest;
import com.backend.cuutro.dto.request.ViTriInputRequest;
import com.backend.cuutro.dto.response.entities.DoiNhomDto;
import com.backend.cuutro.dto.response.entities.DoiNhomThanhVienDto;
import com.backend.cuutro.entities.DoiNhomEntity;
import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.TaiKhoanEntity;
import com.backend.cuutro.entities.TinhNguyenVienEntity;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.DoiNhomMapper;
import com.backend.cuutro.repository.DoiNhomRepository;
import com.backend.cuutro.repository.DoiNhomTinhNguyenVienRepository;
import com.backend.cuutro.repository.TaiKhoanRepository;
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
	private final TaiKhoanRepository taiKhoanRepository;
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
		validateDoiTruongCoTheLamTruongNhom(null, doiTruong);

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
		capNhatVaiTroTaiKhoan(doiTruong, RoleType.TRUONG_NHOM_TNV);
		DoiNhomDto dto = doiNhomMapper.toDto(savedDoiNhom);

		DoiNhomThanhVienDto doiTruongDto = toThanhVienDto(phanCongVaiTro);
		dto.setDoiTruong(doiTruongDto);
		dto.setThanhViens(doiTruongDto == null ? List.of() : List.of(doiTruongDto));
		dto.setSoLuongThanhVien(doiTruongDto == null ? 0 : 1);
		return dto;
	}

	@Override
	@Transactional
	public DoiNhomDto capNhatDoiNhom(Long id, DoiNhomCapNhatRequest request) {
		DoiNhomEntity doiNhom = getDoiNhomOrThrow(id);
		TinhNguyenVienEntity doiTruongMoi = getTinhNguyenVienOrThrow(request.getDoiTruongTinhNguyenVienId());
		validateDoiTruongCoTheLamTruongNhom(id, doiTruongMoi);

		capNhatThongTinDoiNhom(doiNhom, request);
		capNhatVaiTroDoiTruong(doiNhom, doiTruongMoi);

		DoiNhomEntity updated = doiNhomRepository.save(doiNhom);
		return toDoiNhomDtoWithThanhVien(updated);
	}

	@Override
	@Transactional
	public DoiNhomDto capNhatActive(Long id, Boolean active) {
		DoiNhomEntity doiNhom = getDoiNhomOrThrow(id);
		boolean nextActive = Boolean.TRUE.equals(active);
		doiNhom.setActive(nextActive);
		doiNhom.setTrangThaiHoatDong(nextActive);

		DoiNhomEntity updated = doiNhomRepository.save(doiNhom);
		return toDoiNhomDtoWithThanhVien(updated);
	}

	private TinhNguyenVienEntity getTinhNguyenVienOrThrow(Long id) {
		return tinhNguyenVienRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("TinhNguyenVien not found with id=" + id));
	}

	private DoiNhomEntity getDoiNhomOrThrow(Long id) {
		return doiNhomRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("DoiNhom not found with id=" + id));
	}

	private void validateDoiTruongCoTheLamTruongNhom(Long doiNhomId, TinhNguyenVienEntity doiTruong) {
		if (!TrangThaiDuyetTinhNguyenVien.DUOC_DUYET.name().equals(doiTruong.getTrangThaiDuyet())) {
			throw new InvalidFieldException("Chi tinh nguyen vien DUOC_DUYET moi duoc lam doi truong");
		}

		boolean laDoiTruongDoiKhac = doiNhomId == null
				? doiNhomTinhNguyenVienRepository.existsByTinhNguyenVien_IdAndVaiTro(doiTruong.getId(), "truong_nhom")
				: doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdNotAndTinhNguyenVien_IdAndVaiTro(
						doiNhomId,
						doiTruong.getId(),
						"truong_nhom");
		if (laDoiTruongDoiKhac) {
			throw new InvalidFieldException("Nguoi duoc chon da la doi truong cua doi khac");
		}

		boolean daNamTrongDoi = doiNhomId != null
				&& doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndTinhNguyenVien_Id(doiNhomId, doiTruong.getId());
		if (!daNamTrongDoi && doiNhomTinhNguyenVienRepository.countByTinhNguyenVien_Id(doiTruong.getId()) >= 1) {
			throw new InvalidFieldException("Mot tinh nguyen vien chi duoc tham gia toi da 1 doi nhom");
		}
	}

	private void capNhatThongTinDoiNhom(DoiNhomEntity doiNhom, DoiNhomCapNhatRequest request) {
		doiNhom.setTenDoiNhom(request.getTenDoiNhom().trim());
		doiNhom.setSoDienThoai(request.getSoDienThoai().trim());

		ViTriInputRequest viTriRequest = request.getViTri();
		ViTriEntity viTri = doiNhom.getViTri();
		if (viTri == null) {
			doiNhom.setViTri(viTriCommandService.taoViTriMoi(viTriRequest));
			return;
		}

		viTri.setDiaChi(normalizeRequired(viTriRequest.getDiaChi(), "viTri.diaChi is required"));
		viTri.setLat(normalizeNullable(viTriRequest.getLat()));
		viTri.setLongitude(normalizeNullable(viTriRequest.getLongitude()));
	}

	private void capNhatVaiTroDoiTruong(DoiNhomEntity doiNhom, TinhNguyenVienEntity doiTruongMoi) {
		if (doiNhom.getId() == null) {
			return;
		}

		Long doiNhomId = doiNhom.getId();
		Optional<DoiNhomTinhNguyenVienEntity> doiTruongHienTai = doiNhomTinhNguyenVienRepository
				.findFirstByDoiNhom_IdAndVaiTro(doiNhomId, "truong_nhom");
		Optional<DoiNhomTinhNguyenVienEntity> thanhVienMoi = doiNhomTinhNguyenVienRepository
				.findByDoiNhom_IdAndTinhNguyenVien_Id(doiNhomId, doiTruongMoi.getId());
		TinhNguyenVienEntity doiTruongCu = doiTruongHienTai
				.map(DoiNhomTinhNguyenVienEntity::getTinhNguyenVien)
				.orElse(null);

		DoiNhomTinhNguyenVienEntity recordDoiTruongMoi = thanhVienMoi.orElseGet(() -> {
			DoiNhomTinhNguyenVienEntity mapping = new DoiNhomTinhNguyenVienEntity();
			mapping.setDoiNhom(doiNhom);
			mapping.setTinhNguyenVien(doiTruongMoi);
			return doiNhomTinhNguyenVienRepository.save(mapping);
		});
		recordDoiTruongMoi.setVaiTro("truong_nhom");

		if (doiTruongHienTai.isPresent()) {
			DoiNhomTinhNguyenVienEntity current = doiTruongHienTai.get();
			Long currentLeaderId = current.getTinhNguyenVien() != null ? current.getTinhNguyenVien().getId() : null;
			if (!Objects.equals(currentLeaderId, doiTruongMoi.getId())) {
				current.setVaiTro("thanh_vien");
			}
		}
		capNhatVaiTroTaiKhoan(doiTruongMoi, RoleType.TRUONG_NHOM_TNV);
		dongBoVaiTroTaiKhoanDoiTruongCu(doiNhomId, doiTruongCu, doiTruongMoi);
	}

	private void dongBoVaiTroTaiKhoanDoiTruongCu(
			Long doiNhomId,
			TinhNguyenVienEntity doiTruongCu,
			TinhNguyenVienEntity doiTruongMoi
	) {
		if (doiTruongCu == null || doiTruongMoi == null || Objects.equals(doiTruongCu.getId(), doiTruongMoi.getId())) {
			return;
		}
		boolean conLaDoiTruongDoiKhac = doiNhomTinhNguyenVienRepository
				.existsByTinhNguyenVien_IdAndDoiNhom_IdNotAndVaiTro(doiTruongCu.getId(), doiNhomId, "truong_nhom");
		if (conLaDoiTruongDoiKhac) {
			return;
		}
		capNhatVaiTroTaiKhoan(doiTruongCu, RoleType.NGUOI_DAN);
	}

	private void capNhatVaiTroTaiKhoan(TinhNguyenVienEntity tinhNguyenVien, RoleType vaiTro) {
		if (tinhNguyenVien == null || vaiTro == null || tinhNguyenVien.getNguoiDung() == null) {
			return;
		}
		TaiKhoanEntity taiKhoan = tinhNguyenVien.getNguoiDung().getTaiKhoan();
		if (taiKhoan == null) {
			return;
		}
		if (vaiTro.equals(taiKhoan.getVaiTro())) {
			return;
		}
		taiKhoan.setVaiTro(vaiTro);
		taiKhoanRepository.save(taiKhoan);
	}

	private String normalizeRequired(String value, String errorMessage) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(errorMessage);
		}
		return value.trim();
	}

	private String normalizeNullable(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		return value.trim();
	}

	private DoiNhomDto toDoiNhomDtoWithThanhVien(DoiNhomEntity doiNhom) {
		DoiNhomDto dto = doiNhomMapper.toDto(doiNhom);
		if (doiNhom.getId() == null) {
			dto.setThanhViens(List.of());
			dto.setSoLuongThanhVien(0);
			dto.setDoiTruong(null);
			return dto;
		}

		List<DoiNhomThanhVienDto> thanhViens = doiNhomTinhNguyenVienRepository
				.findByDoiNhom_IdInOrderByCreatedAtAsc(List.of(doiNhom.getId()))
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
		return dto;
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
				.viTri(toViTriDto(nguoiDung != null ? nguoiDung.getViTri() : null))
				.vaiTro(vaiTro)
				.build();
	}

	private com.backend.cuutro.dto.response.entities.ViTriDto toViTriDto(ViTriEntity viTri) {
		if (viTri == null) {
			return null;
		}
		return com.backend.cuutro.dto.response.entities.ViTriDto.builder()
				.id(viTri.getId())
				.lat(viTri.getLat())
				.longitude(viTri.getLongitude())
				.diaChi(viTri.getDiaChi())
				.createdAt(viTri.getCreatedAt())
				.build();
	}
}
