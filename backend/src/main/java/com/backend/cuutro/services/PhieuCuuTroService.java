package com.backend.cuutro.services;

import java.util.ArrayList;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.LoaiNguoiGui;
import com.backend.cuutro.constant.enums.RoleType;
import com.backend.cuutro.constant.enums.TrangThaiDoiNhom;
import com.backend.cuutro.constant.enums.TrangThaiPhieuHoTro;
import com.backend.cuutro.dto.request.CapNhatTrangThaiPhieuRequest;
import com.backend.cuutro.dto.request.CapNhatDaXemTinNhanRequest;
import com.backend.cuutro.dto.request.CapNhatTrangThaiDangGoTinNhanRequest;
import com.backend.cuutro.dto.request.DieuPhoiPhieuRequest;
import com.backend.cuutro.dto.request.GuiTinNhanPhieuRequest;
import com.backend.cuutro.dto.request.NguoiGuiRequest;
import com.backend.cuutro.dto.request.PhieuCuuTroFilterRequest;
import com.backend.cuutro.dto.request.TaoChiTietCuuTroRequest;
import com.backend.cuutro.dto.request.TaoPhieuHoTroRequest;
import com.backend.cuutro.dto.request.TaoPhieuHoTroTepTinRequest;
import com.backend.cuutro.dto.response.entities.NguoiGuiDto;
import com.backend.cuutro.dto.response.entities.NguoiDungDto;
import com.backend.cuutro.dto.response.entities.DoiNhomThanhVienDto;
import com.backend.cuutro.dto.response.entities.PhanCongDto;
import com.backend.cuutro.dto.response.entities.PhieuCuuTroChiTietDto;
import com.backend.cuutro.dto.response.entities.PhieuCuuTroDto;
import com.backend.cuutro.dto.response.entities.TaiKhoanDto;
import com.backend.cuutro.dto.response.entities.TrangThaiDangGoTinNhanResponse;
import com.backend.cuutro.dto.response.entities.TinNhanChuaDocResponse;
import com.backend.cuutro.dto.response.entities.TinNhanDaXemResponse;
import com.backend.cuutro.dto.response.entities.TinNhanDto;
import com.backend.cuutro.dto.response.entities.TrangThaiPhieuResponse;
import com.backend.cuutro.dto.response.entities.ViTriDto;
import com.backend.cuutro.entities.ChiTietCuuTroEntity;
import com.backend.cuutro.entities.DoiNhomEntity;
import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;
import com.backend.cuutro.entities.NguoiDungEntity;
import com.backend.cuutro.entities.PhanCongEntity;
import com.backend.cuutro.entities.PhieuCuuTroEntity;
import com.backend.cuutro.entities.PhieuCuuTroTepTinEntity;
import com.backend.cuutro.entities.TepTinEntity;
import com.backend.cuutro.entities.TinhNguyenVienEntity;
import com.backend.cuutro.entities.TinNhanDaXemEntity;
import com.backend.cuutro.entities.TinNhanEntity;
import com.backend.cuutro.entities.VatPhamEntity;
import com.backend.cuutro.entities.ViTriEntity;
import com.backend.cuutro.exception.customize.InvalidFieldException;
import com.backend.cuutro.mapper.PhanCongMapper;
import com.backend.cuutro.mapper.PhieuCuuTroMapper;
import com.backend.cuutro.mapper.TinNhanMapper;
import com.backend.cuutro.repository.ChiTietCuuTroRepository;
import com.backend.cuutro.repository.DoiNhomRepository;
import com.backend.cuutro.repository.DoiNhomTinhNguyenVienRepository;
import com.backend.cuutro.repository.LoaiSuCoRepository;
import com.backend.cuutro.repository.NguoiDungRepository;
import com.backend.cuutro.repository.PhanCongRepository;
import com.backend.cuutro.repository.PhieuCuuTroRepository;
import com.backend.cuutro.repository.PhieuCuuTroTepTinRepository;
import com.backend.cuutro.repository.TepTinRepository;
import com.backend.cuutro.repository.TinNhanDaXemRepository;
import com.backend.cuutro.repository.TinNhanRepository;
import com.backend.cuutro.repository.VatPhamRepository;
import com.backend.cuutro.repository.ViTriRepository;
import com.backend.cuutro.repository.specification.PhieuCuuTroSpecifications;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhieuCuuTroService {

	private static final String DOI_TRUONG_VAI_TRO = "truong_nhom";
	private static final String DOI_NHOM_TRANG_THAI_RANH_DB = "idle";
	private static final String DOI_NHOM_TRANG_THAI_HOAT_DONG_DB = "busy";
	private static final String PHIEU_TRANG_THAI_PENDING_DB = "pending";
	private static final String PHIEU_TRANG_THAI_ASSIGNED_DB = "assigned";
	private static final String PHIEU_TRANG_THAI_PROCESSING_DB = "processing";
	private static final String PHIEU_TRANG_THAI_DONE_DB = "done";
	private static final String LOAI_TEP_TIN_MAC_DINH = "general";
	private static final String LOAI_TIN_NHAN_TEXT = "text";
	private static final String LOAI_TIN_NHAN_MEDIA = "media";
	private static final String LOAI_TIN_NHAN_LOCATION = "location";
	private static final String LOAI_TIN_NHAN_MIXED = "mixed";
	private static final List<String> TRANG_THAI_PHIEU_KET_THUC = List.of(
			"completed",
			"done",
			TrangThaiPhieuHoTro.HOAN_THANH.name(),
			TrangThaiPhieuHoTro.HUY.name());

	private final PhieuCuuTroRepository phieuCuuTroRepository;
	private final ChiTietCuuTroRepository chiTietCuuTroRepository;
	private final PhanCongRepository phanCongRepository;
	private final DoiNhomRepository doiNhomRepository;
	private final DoiNhomTinhNguyenVienRepository doiNhomTinhNguyenVienRepository;
	private final LoaiSuCoRepository loaiSuCoRepository;
	private final ViTriRepository viTriRepository;
	private final PhieuCuuTroTepTinRepository phieuCuuTroTepTinRepository;
	private final TepTinRepository tepTinRepository;
	private final VatPhamRepository vatPhamRepository;
	private final NguoiDungRepository nguoiDungRepository;
	private final TinNhanRepository tinNhanRepository;
	private final TinNhanDaXemRepository tinNhanDaXemRepository;
	private final ViTriCommandService viTriCommandService;
	private final TinNhanRealtimePublisher tinNhanRealtimePublisher;
	private final PhieuCuuTroMapper phieuCuuTroMapper;
	private final PhanCongMapper phanCongMapper;
	private final TinNhanMapper tinNhanMapper;

	@Transactional
	public PhieuCuuTroDto taoPhieuCongKhai(TaoPhieuHoTroRequest request) {
		PhieuCuuTroEntity entity = new PhieuCuuTroEntity();
		ResolvedNguoiGui resolvedNguoiGui = resolveNguoiGui(request.getNguoiGui());
		entity.setLoaiSuCo(loaiSuCoRepository.findById(request.getLoaiSuCoId())
				.orElseThrow(() -> new EntityNotFoundException("LoaiSuCo not found with id=" + request.getLoaiSuCoId())));
		entity.setViTri(resolveViTriTaoPhieu(request));
		entity.setNguoiDung(resolvedNguoiGui.nguoiDung());
		entity.setHoTen(resolvedNguoiGui.ten());
		entity.setSdt(resolvedNguoiGui.sdt());
		entity.setGhiChu(request.getGhiChu().trim());
		entity.setTrangThai(PHIEU_TRANG_THAI_PENDING_DB);
		PhieuCuuTroEntity savedPhieu = phieuCuuTroRepository.save(entity);

		List<ChiTietCuuTroEntity> chiTietCuuTro = buildChiTietCuuTro(savedPhieu, request.getChiTietCuuTro());
		chiTietCuuTroRepository.saveAll(chiTietCuuTro);

		List<TaoPhieuHoTroTepTinRequest> tepTinRequests = resolveTepTinRequests(request);
		List<PhieuCuuTroTepTinEntity> phieuCuuTroTepTinEntities = buildPhieuCuuTroTepTin(savedPhieu, tepTinRequests);
		phieuCuuTroTepTinRepository.saveAll(phieuCuuTroTepTinEntities);
		savedPhieu.setTepTins(phieuCuuTroTepTinEntities);

		PhieuCuuTroDto dto = phieuCuuTroMapper.toDto(savedPhieu);
		dto.setNguoiGui(buildNguoiGuiDto(savedPhieu));
		dto.setChiTietCuuTro(toChiTietDtos(chiTietCuuTro));
		return dto;
	}

	@Transactional
	public PhanCongDto dieuPhoi(Long phieuId, DieuPhoiPhieuRequest request) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		TrangThaiPhieuHoTro trangThaiPhieu = getTrangThaiNghiepVu(
				phieu,
				phanCongRepository.findByPhieuCuuTro_Id(phieuId).orElse(null));
		if (trangThaiPhieu != TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Chi duoc dieu phoi phieu o trang thai CHO_DIEU_PHOI");
		}
		if (phanCongRepository.existsByPhieuCuuTro_Id(phieuId)) {
			throw new InvalidFieldException("Phieu da duoc dieu phoi");
		}

		DoiNhomEntity doiNhom = doiNhomRepository.findById(request.getDoiNhomId())
				.orElseThrow(() -> new EntityNotFoundException("DoiNhom not found with id=" + request.getDoiNhomId()));
		if (resolveTrangThaiDoiNhom(doiNhom) != TrangThaiDoiNhom.DANG_RANH) {
			throw new InvalidFieldException("Chi duoc dieu phoi doi nhom co trang thai DANG_RANH");
		}
		if (phanCongRepository.existsByDoiNhom_IdAndTrangThaiNotIn(
				doiNhom.getId(),
				TRANG_THAI_PHIEU_KET_THUC)) {
			throw new InvalidFieldException("Doi nhom dang co nhiem vu dang xu ly");
		}

		PhanCongEntity phanCong = new PhanCongEntity();
		phanCong.setPhieuCuuTro(phieu);
		phanCong.setDoiNhom(doiNhom);
		phanCong.setTrangThai(TrangThaiPhieuHoTro.CHO_DIEU_PHOI.name());

		doiNhom.setTrangThai(DOI_NHOM_TRANG_THAI_HOAT_DONG_DB);
		doiNhomRepository.save(doiNhom);
		return phanCongMapper.toDto(phanCongRepository.save(phanCong));
	}

	@Transactional
	public TrangThaiPhieuResponse nhanNhiemVu(Long phieuId) {
		PhanCongEntity phanCong = getPhanCongOrThrow(phieuId);
		validateCurrentUserLaDoiTruong(phanCong.getDoiNhom().getId());

		PhieuCuuTroEntity phieu = phanCong.getPhieuCuuTro();
		TrangThaiPhieuHoTro trangThaiHienTai = getTrangThaiNghiepVu(phieu, phanCong);
		if (trangThaiHienTai != TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Chi duoc nhan nhiem vu khi phieu dang CHO_DIEU_PHOI");
		}

		truTonKhoVatPhamChoPhieu(phieu.getId());
		phieu.setTrangThai(PHIEU_TRANG_THAI_ASSIGNED_DB);
		phieuCuuTroRepository.save(phieu);
		phanCong.setTrangThai(TrangThaiPhieuHoTro.DA_NHAN.name());
		phanCongRepository.save(phanCong);
		return buildTrangThaiResponse(phieu, phanCong);
	}

	@Transactional
	public TrangThaiPhieuResponse tuChoiNhiemVu(Long phieuId) {
		PhanCongEntity phanCong = getPhanCongOrThrow(phieuId);
		validateCurrentUserLaDoiTruong(phanCong.getDoiNhom().getId());

		PhieuCuuTroEntity phieu = phanCong.getPhieuCuuTro();
		TrangThaiPhieuHoTro trangThaiHienTai = getTrangThaiNghiepVu(phieu, phanCong);
		if (trangThaiHienTai != TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Chi duoc tu choi nhiem vu khi phieu dang CHO_DIEU_PHOI");
		}

		phieu.setTrangThai(PHIEU_TRANG_THAI_PENDING_DB);
		phieuCuuTroRepository.save(phieu);
		phanCongRepository.delete(phanCong);
		setDoiNhomDangRanh(phanCong.getDoiNhom());
		return buildTrangThaiResponse(phieu, null);
	}

	@Transactional
	public TrangThaiPhieuResponse capNhatTrangThai(Long phieuId, CapNhatTrangThaiPhieuRequest request) {
		PhanCongEntity phanCong = getPhanCongOrThrow(phieuId);
		validateCurrentUserLaDoiTruong(phanCong.getDoiNhom().getId());

		PhieuCuuTroEntity phieu = phanCong.getPhieuCuuTro();
		TrangThaiPhieuHoTro hienTai = getTrangThaiNghiepVu(phieu, phanCong);
		TrangThaiPhieuHoTro moi = parseTrangThai(request.getTrangThai());

		if (hienTai == moi) {
			return buildTrangThaiResponse(phieu, phanCong);
		}
		validateLuotTrangThai(hienTai, moi);
		if (moi == TrangThaiPhieuHoTro.HUY && daTruTonKhoVatPham(hienTai)) {
			congLaiTonKhoVatPhamChoPhieu(phieu.getId());
		}

		phieu.setTrangThai(toDbTrangThai(moi));
		phieuCuuTroRepository.save(phieu);
		phanCong.setTrangThai(moi.name());
		phanCongRepository.save(phanCong);
		if (moi == TrangThaiPhieuHoTro.HOAN_THANH || moi == TrangThaiPhieuHoTro.HUY) {
			setDoiNhomDangRanh(phanCong.getDoiNhom());
		}
		return buildTrangThaiResponse(phieu, phanCong);
	}

	public TrangThaiPhieuResponse getTrangThai(Long phieuId) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		return buildTrangThaiResponse(phieu, phanCongRepository.findByPhieuCuuTro_Id(phieuId).orElse(null));
	}

	public PhieuCuuTroDto getById(Long phieuId) {
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		PhanCongEntity phanCong = phanCongRepository.findByPhieuCuuTro_Id(phieuId).orElse(null);
		PhieuCuuTroDto dto = phieuCuuTroMapper.toDto(phieu);
		dto.setNguoiGui(buildNguoiGuiDto(phieu));
		dto.setTrangThai(getTrangThaiNghiepVu(phieu, phanCong).name());
		dto.setPhanCong(buildPhanCongDtoForResponse(phanCong));
		dto.setChiTietCuuTro(toChiTietDtos(chiTietCuuTroRepository.findByPhieuCuuTro_IdOrderByIdAsc(phieuId)));
		return dto;
	}

	public List<PhieuCuuTroDto> getDanhSach() {
		List<PhieuCuuTroEntity> entities = resolveDanhSachPhieuTheoNguoiDungHienTai();
		List<PhieuCuuTroDto> dtoList = phieuCuuTroMapper.toDtoList(entities);
		for (int i = 0; i < entities.size(); i++) {
			PhieuCuuTroEntity entity = entities.get(i);
			PhanCongEntity phanCong = phanCongRepository.findByPhieuCuuTro_Id(entity.getId()).orElse(null);
			TrangThaiPhieuHoTro trangThai = getTrangThaiNghiepVu(
					entity,
					phanCong);
			dtoList.get(i).setNguoiGui(buildNguoiGuiDto(entity));
			dtoList.get(i).setTrangThai(trangThai.name());
			dtoList.get(i).setPhanCong(buildPhanCongDtoForResponse(phanCong));
			dtoList.get(i).setChiTietCuuTro(
					toChiTietDtos(chiTietCuuTroRepository.findByPhieuCuuTro_IdOrderByIdAsc(entity.getId())));
		}
		return dtoList;
	}

	private PhanCongDto buildPhanCongDtoForResponse(PhanCongEntity phanCong) {
		if (phanCong == null) {
			return null;
		}
		PhanCongDto dto = phanCongMapper.toDto(phanCong);
		if (dto == null) {
			return null;
		}
		dto.setPhieuCuuTro(null);
		enrichCaptainIntoAssignedTeam(dto);
		return dto;
	}

	private void enrichCaptainIntoAssignedTeam(PhanCongDto phanCongDto) {
		if (phanCongDto.getDoiNhom() == null || phanCongDto.getDoiNhom().getId() == null) {
			return;
		}
		Long doiNhomId = phanCongDto.getDoiNhom().getId();
		doiNhomTinhNguyenVienRepository.findFirstByDoiNhom_IdAndVaiTro(doiNhomId, DOI_TRUONG_VAI_TRO)
				.ifPresent(leaderMapping -> phanCongDto.getDoiNhom().setDoiTruong(toDoiTruongDto(leaderMapping)));
	}

	private DoiNhomThanhVienDto toDoiTruongDto(DoiNhomTinhNguyenVienEntity leaderMapping) {
		if (leaderMapping == null || leaderMapping.getTinhNguyenVien() == null) {
			return null;
		}
		TinhNguyenVienEntity tinhNguyenVien = leaderMapping.getTinhNguyenVien();
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
		return DoiNhomThanhVienDto.builder()
				.tinhNguyenVienId(tinhNguyenVien.getId())
				.ten(ten)
				.sdt(sdt)
				.avatarUrl(avatarUrl)
				.viTri(toViTriDto(nguoiDung != null ? nguoiDung.getViTri() : null))
				.vaiTro(DOI_TRUONG_VAI_TRO)
				.build();
	}

	private ViTriDto toViTriDto(ViTriEntity viTri) {
		if (viTri == null) {
			return null;
		}
		return ViTriDto.builder()
				.id(viTri.getId())
				.lat(viTri.getLat())
				.longitude(viTri.getLongitude())
				.diaChi(viTri.getDiaChi())
				.createdAt(viTri.getCreatedAt())
				.build();
	}

	private List<PhieuCuuTroEntity> resolveDanhSachPhieuTheoNguoiDungHienTai() {
		Set<String> roles = getCurrentRoles();
		if (roles.contains(RoleType.ADMIN.name())) {
			return phieuCuuTroRepository.findAllByOrderByCreatedAtDesc();
		}
		if (roles.contains(RoleType.NGUOI_DAN.name())) {
			Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
			NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
			return phieuCuuTroRepository.findAllByNguoiDung_IdOrderByCreatedAtDesc(nguoiDung.getId());
		}
		if (roles.contains(RoleType.TRUONG_NHOM_TNV.name())) {
			return resolveDanhSachPhieuChoDoiTruong();
		}
		return phieuCuuTroRepository.findAllByOrderByCreatedAtDesc();
	}

	private List<PhieuCuuTroEntity> resolveDanhSachPhieuChoDoiTruong() {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		List<Long> doiNhomIds = doiNhomTinhNguyenVienRepository
				.findByTinhNguyenVien_NguoiDung_IdAndVaiTro(nguoiDung.getId(), DOI_TRUONG_VAI_TRO)
				.stream()
				.map(mapping -> mapping.getDoiNhom() != null ? mapping.getDoiNhom().getId() : null)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		if (doiNhomIds.isEmpty()) {
			return List.of();
		}

		return phanCongRepository.findAllByDoiNhom_IdInOrderByAssignedAtDesc(doiNhomIds)
				.stream()
				.map(PhanCongEntity::getPhieuCuuTro)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
	}
 
	@Transactional
	public TinNhanDto guiTinNhan(Long phieuId, GuiTinNhanPhieuRequest request) {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		return guiTinNhan(phieuId, request, taiKhoanId, getCurrentRoles());
	}

	@Transactional
	public TinNhanDto guiTinNhan(Long phieuId, GuiTinNhanPhieuRequest request, Long taiKhoanId, Set<String> roles) {
		if (request == null) {
			throw new InvalidFieldException("request is required");
		}
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}
		String noiDung = StringUtils.hasText(request.getNoiDung()) ? request.getNoiDung().trim() : null;
		TepTinEntity tepTin = resolveTepTinGuiTinNhan(request);
		ViTriEntity viTri = resolveViTriGuiTinNhan(request);

		boolean coNoiDung = StringUtils.hasText(noiDung);
		boolean coMedia = tepTin != null;
		boolean coViTri = viTri != null;
		if (!coNoiDung && !coMedia && !coViTri) {
			throw new InvalidFieldException("Tin nhan phai co it nhat mot trong cac truong: noiDung, tepTinId, viTriId, viTri");
		}

		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		NguoiDungEntity sender = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, sender, roles);

		TinNhanEntity entity = new TinNhanEntity();
		entity.setPhieuCuuTro(phieu);
		entity.setSender(sender);
		entity.setViTri(viTri);
		entity.setNoiDung(noiDung);
		entity.setLoaiTinNhan(resolveLoaiTinNhan(coNoiDung, coMedia, coViTri));
		entity.setMediaUrl(tepTin != null ? tepTin.getDuongDan() : null);
		entity.setMediaType(tepTin != null ? tepTin.getLoaiTepTin() : null);
		TinNhanDto tinNhanDto = compactTinNhanForResponse(tinNhanMapper.toDto(tinNhanRepository.save(entity)));
		tinNhanRealtimePublisher.publishTinNhan(phieuId, tinNhanDto);
		return tinNhanDto;
	}

	public List<TinNhanDto> getDanhSachTinNhan(Long phieuId) {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		return getDanhSachTinNhan(phieuId, taiKhoanId, getCurrentRoles());
	}

	public List<TinNhanDto> getDanhSachTinNhan(Long phieuId, Long taiKhoanId, Set<String> roles) {
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		NguoiDungEntity sender = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, sender, roles);
		return tinNhanRepository.findByPhieuCuuTro_IdOrderByCreatedAtAsc(phieuId)
				.stream()
				.map(tinNhanMapper::toDto)
				.map(this::compactTinNhanForResponse)
				.toList();
	}

	private TinNhanDto compactTinNhanForResponse(TinNhanDto dto) {
		if (dto == null) {
			return null;
		}
		dto.setPhieuCuuTro(null);
		dto.setSender(compactSenderForTinNhan(dto.getSender()));
		return dto;
	}

	private NguoiDungDto compactSenderForTinNhan(NguoiDungDto sender) {
		if (sender == null) {
			return null;
		}
		return NguoiDungDto.builder()
				.id(sender.getId())
				.ten(StringUtils.hasText(sender.getTen()) ? sender.getTen().trim() : "")
				.avatarUrl(StringUtils.hasText(sender.getAvatarUrl()) ? sender.getAvatarUrl().trim() : "")
				.taiKhoan(compactTaiKhoanForTinNhan(sender.getTaiKhoan()))
				.build();
	}

	private TaiKhoanDto compactTaiKhoanForTinNhan(TaiKhoanDto taiKhoan) {
		if (taiKhoan == null) {
			return null;
		}
		return TaiKhoanDto.builder()
				.id(taiKhoan.getId())
				.tenDangNhap(StringUtils.hasText(taiKhoan.getTenDangNhap()) ? taiKhoan.getTenDangNhap().trim() : "")
				.vaiTro(taiKhoan.getVaiTro())
				.build();
	}

	public void validateCoQuyenChat(Long phieuId, Long taiKhoanId, Set<String> roles) {
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}
		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		NguoiDungEntity sender = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, sender, roles);
	}

	@Transactional
	public TinNhanDaXemResponse capNhatDaXemTinNhan(Long phieuId, CapNhatDaXemTinNhanRequest request) {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		return capNhatDaXemTinNhan(phieuId, request, taiKhoanId, getCurrentRoles());
	}

	@Transactional
	public TinNhanDaXemResponse capNhatDaXemTinNhan(
			Long phieuId,
			CapNhatDaXemTinNhanRequest request,
			Long taiKhoanId,
			Set<String> roles) {
		if (request == null) {
			throw new InvalidFieldException("request is required");
		}
		if (request.getLastSeenMessageId() == null || request.getLastSeenMessageId() <= 0) {
			throw new InvalidFieldException("lastSeenMessageId is required");
		}
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}

		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, nguoiDung, roles);

		TinNhanEntity lastSeenTinNhan = tinNhanRepository.findById(request.getLastSeenMessageId())
				.orElseThrow(() -> new EntityNotFoundException("TinNhan not found with id=" + request.getLastSeenMessageId()));
		if (lastSeenTinNhan.getPhieuCuuTro() == null
				|| !Objects.equals(lastSeenTinNhan.getPhieuCuuTro().getId(), phieuId)) {
			throw new InvalidFieldException("Tin nhan khong thuoc phieu cuu tro hien tai");
		}

		TinNhanDaXemEntity tinNhanDaXem = tinNhanDaXemRepository
				.findByPhieuCuuTro_IdAndNguoiDung_Id(phieuId, nguoiDung.getId())
				.orElseGet(() -> {
					TinNhanDaXemEntity entity = new TinNhanDaXemEntity();
					entity.setPhieuCuuTro(phieu);
					entity.setNguoiDung(nguoiDung);
					return entity;
				});
		Instant now = Instant.now();
		tinNhanDaXem.setLastSeenMessage(lastSeenTinNhan);
		tinNhanDaXem.setLastSeenAt(now);
		TinNhanDaXemEntity saved = tinNhanDaXemRepository.save(tinNhanDaXem);

		TinNhanDaXemResponse response = TinNhanDaXemResponse.builder()
				.phieuId(phieuId)
				.nguoiDungId(nguoiDung.getId())
				.lastSeenMessageId(saved.getLastSeenMessage() != null ? saved.getLastSeenMessage().getId() : null)
				.lastSeenAt(saved.getLastSeenAt())
				.build();
		tinNhanRealtimePublisher.publishTinNhanDaXem(phieuId, response);
		return response;
	}

	public TinNhanChuaDocResponse getTinNhanChuaDoc(Long phieuId) {
		Long taiKhoanId = getCurrentTaiKhoanIdOrThrow();
		return getTinNhanChuaDoc(phieuId, taiKhoanId, getCurrentRoles());
	}

	public TinNhanChuaDocResponse getTinNhanChuaDoc(Long phieuId, Long taiKhoanId, Set<String> roles) {
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}

		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, nguoiDung, roles);

		TinNhanDaXemEntity tinNhanDaXem = tinNhanDaXemRepository
				.findByPhieuCuuTro_IdAndNguoiDung_Id(phieuId, nguoiDung.getId())
				.orElse(null);
		Long lastSeenMessageId = tinNhanDaXem != null && tinNhanDaXem.getLastSeenMessage() != null
				? tinNhanDaXem.getLastSeenMessage().getId()
				: null;
		Instant lastSeenAt = tinNhanDaXem != null ? tinNhanDaXem.getLastSeenAt() : null;
		long soLuongChuaDoc = tinNhanDaXemRepository.countTinNhanChuaDoc(phieuId, nguoiDung.getId(), lastSeenMessageId);
		return TinNhanChuaDocResponse.builder()
				.phieuId(phieuId)
				.nguoiDungId(nguoiDung.getId())
				.soLuongChuaDoc(soLuongChuaDoc)
				.lastSeenMessageId(lastSeenMessageId)
				.lastSeenAt(lastSeenAt)
				.build();
	}

	public TrangThaiDangGoTinNhanResponse capNhatTrangThaiDangGoTinNhan(
			Long phieuId,
			CapNhatTrangThaiDangGoTinNhanRequest request,
			Long taiKhoanId,
			Set<String> roles) {
		if (request == null || request.getDangGo() == null) {
			throw new InvalidFieldException("dangGo is required");
		}
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}

		PhieuCuuTroEntity phieu = getPhieuOrThrow(phieuId);
		NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(taiKhoanId);
		validateCoQuyenChat(phieu, nguoiDung, roles);

		TrangThaiDangGoTinNhanResponse response = TrangThaiDangGoTinNhanResponse.builder()
				.phieuId(phieuId)
				.nguoiDungId(nguoiDung.getId())
				.dangGo(request.getDangGo())
				.updatedAt(Instant.now())
				.build();
		tinNhanRealtimePublisher.publishTrangThaiDangGo(phieuId, response);
		return response;
	}

	public Page<PhieuCuuTroEntity> search(PhieuCuuTroFilterRequest filter, Pageable pageable) {
		return phieuCuuTroRepository.findAll(PhieuCuuTroSpecifications.withFilter(filter), pageable);
	}

	public List<PhieuCuuTroEntity> search(PhieuCuuTroFilterRequest filter) {
		return phieuCuuTroRepository.findAll(PhieuCuuTroSpecifications.withFilter(filter));
	}

	private PhieuCuuTroEntity getPhieuOrThrow(Long phieuId) {
		return phieuCuuTroRepository.findById(phieuId)
				.orElseThrow(() -> new EntityNotFoundException("PhieuCuuTro not found with id=" + phieuId));
	}

	private PhanCongEntity getPhanCongOrThrow(Long phieuId) {
		return phanCongRepository.findByPhieuCuuTro_Id(phieuId)
				.orElseThrow(() -> new InvalidFieldException("Phieu chua duoc dieu phoi doi nhom"));
	}

	private TepTinEntity resolveTepTinGuiTinNhan(GuiTinNhanPhieuRequest request) {
		if (request.getTepTinId() == null) {
			return null;
		}
		return tepTinRepository.findById(request.getTepTinId())
				.orElseThrow(() -> new EntityNotFoundException("TepTin not found with id=" + request.getTepTinId()));
	}

	private ViTriEntity resolveViTriGuiTinNhan(GuiTinNhanPhieuRequest request) {
		if (request.getViTriId() != null && request.getViTri() != null) {
			throw new InvalidFieldException("Chi duoc truyen mot trong hai truong: viTriId hoac viTri");
		}
		if (request.getViTriId() != null) {
			return viTriRepository.findById(request.getViTriId())
					.orElseThrow(() -> new EntityNotFoundException("ViTri not found with id=" + request.getViTriId()));
		}
		if (request.getViTri() != null) {
			return viTriCommandService.taoViTriMoi(request.getViTri());
		}
		return null;
	}

	private ViTriEntity resolveViTriTaoPhieu(TaoPhieuHoTroRequest request) {
		if (request.getViTriId() != null && request.getViTri() != null) {
			throw new InvalidFieldException("Chi duoc truyen mot trong hai truong: viTriId hoac viTri");
		}
		if (request.getViTriId() != null) {
			return viTriRepository.findById(request.getViTriId())
					.orElseThrow(() -> new EntityNotFoundException("ViTri not found with id=" + request.getViTriId()));
		}
		if (request.getViTri() != null) {
			return viTriCommandService.taoViTriMoi(request.getViTri());
		}
		throw new InvalidFieldException("viTriId hoac viTri is required");
	}

	private String resolveLoaiTinNhan(boolean coNoiDung, boolean coMedia, boolean coViTri) {
		int soThanhPhan = 0;
		if (coNoiDung) {
			soThanhPhan++;
		}
		if (coMedia) {
			soThanhPhan++;
		}
		if (coViTri) {
			soThanhPhan++;
		}
		if (soThanhPhan > 1) {
			return LOAI_TIN_NHAN_MIXED;
		}
		if (coMedia) {
			return LOAI_TIN_NHAN_MEDIA;
		}
		if (coViTri) {
			return LOAI_TIN_NHAN_LOCATION;
		}
		return LOAI_TIN_NHAN_TEXT;
	}

	private List<ChiTietCuuTroEntity> buildChiTietCuuTro(PhieuCuuTroEntity phieu, List<TaoChiTietCuuTroRequest> chiTietRequests) {
		Set<Long> vatPhamDaThem = new HashSet<>();
		List<ChiTietCuuTroEntity> chiTietEntities = new ArrayList<>(chiTietRequests.size());

		for (TaoChiTietCuuTroRequest chiTietRequest : chiTietRequests) {
			Long vatPhamId = chiTietRequest.getVatPhamId();
			if (!vatPhamDaThem.add(vatPhamId)) {
				throw new InvalidFieldException("Khong duoc trung vatPhamId trong chiTietCuuTro");
			}

			ChiTietCuuTroEntity chiTietEntity = new ChiTietCuuTroEntity();
			chiTietEntity.setPhieuCuuTro(phieu);
			chiTietEntity.setVatPham(vatPhamRepository.findById(vatPhamId)
					.orElseThrow(() -> new EntityNotFoundException("VatPham not found with id=" + vatPhamId)));
			chiTietEntity.setSoLuong(chiTietRequest.getSoLuong());
			chiTietEntity.setGhiChu(StringUtils.hasText(chiTietRequest.getGhiChu()) ? chiTietRequest.getGhiChu().trim() : null);
			chiTietEntities.add(chiTietEntity);
		}

		return chiTietEntities;
	}

	private void truTonKhoVatPhamChoPhieu(Long phieuId) {
		Map<Long, Integer> soLuongYeuCauTheoVatPham = tongHopSoLuongVatPhamTheoPhieu(phieuId);
		List<VatPhamEntity> vatPhamCanKhoa = vatPhamRepository.findAllByIdInForUpdate(soLuongYeuCauTheoVatPham.keySet());
		Map<Long, VatPhamEntity> vatPhamById = vatPhamCanKhoa.stream()
				.collect(Collectors.toMap(VatPhamEntity::getId, item -> item));

		for (Map.Entry<Long, Integer> entry : soLuongYeuCauTheoVatPham.entrySet()) {
			Long vatPhamId = entry.getKey();
			VatPhamEntity vatPham = vatPhamById.get(vatPhamId);
			if (vatPham == null) {
				throw new EntityNotFoundException("VatPham not found with id=" + vatPhamId);
			}

			int tonKhoHienTai = vatPham.getSoLuong() == null ? 0 : vatPham.getSoLuong();
			if (tonKhoHienTai < 0) {
				throw new InvalidFieldException("So luong ton kho khong hop le cho vat pham id=" + vatPhamId);
			}

			int soLuongYeuCau = entry.getValue();
			if (tonKhoHienTai < soLuongYeuCau) {
				String tenVatPham = StringUtils.hasText(vatPham.getTenVatPham()) ? vatPham.getTenVatPham().trim()
						: ("VatPham#" + vatPhamId);
				throw new InvalidFieldException(
						"Khong du ton kho cho vat pham '" + tenVatPham + "'. Con lai: " + tonKhoHienTai
								+ ", yeu cau: " + soLuongYeuCau);
			}

			vatPham.setSoLuong((short) (tonKhoHienTai - soLuongYeuCau));
		}

		vatPhamRepository.saveAll(vatPhamCanKhoa);
	}

	private void congLaiTonKhoVatPhamChoPhieu(Long phieuId) {
		Map<Long, Integer> soLuongYeuCauTheoVatPham = tongHopSoLuongVatPhamTheoPhieu(phieuId);
		List<VatPhamEntity> vatPhamCanKhoa = vatPhamRepository.findAllByIdInForUpdate(soLuongYeuCauTheoVatPham.keySet());
		Map<Long, VatPhamEntity> vatPhamById = vatPhamCanKhoa.stream()
				.collect(Collectors.toMap(VatPhamEntity::getId, item -> item));

		for (Map.Entry<Long, Integer> entry : soLuongYeuCauTheoVatPham.entrySet()) {
			Long vatPhamId = entry.getKey();
			VatPhamEntity vatPham = vatPhamById.get(vatPhamId);
			if (vatPham == null) {
				throw new EntityNotFoundException("VatPham not found with id=" + vatPhamId);
			}

			int tonKhoHienTai = vatPham.getSoLuong() == null ? 0 : vatPham.getSoLuong();
			if (tonKhoHienTai < 0) {
				throw new InvalidFieldException("So luong ton kho khong hop le cho vat pham id=" + vatPhamId);
			}

			int soLuongYeuCau = entry.getValue();
			int tonKhoMoi = tonKhoHienTai + soLuongYeuCau;
			if (tonKhoMoi > Short.MAX_VALUE) {
				throw new InvalidFieldException("So luong ton kho vuot qua gioi han cho vat pham id=" + vatPhamId);
			}
			vatPham.setSoLuong((short) tonKhoMoi);
		}

		vatPhamRepository.saveAll(vatPhamCanKhoa);
	}

	private Map<Long, Integer> tongHopSoLuongVatPhamTheoPhieu(Long phieuId) {
		List<ChiTietCuuTroEntity> chiTietList = chiTietCuuTroRepository.findByPhieuCuuTro_IdOrderByIdAsc(phieuId);
		if (chiTietList == null || chiTietList.isEmpty()) {
			throw new InvalidFieldException("Phieu khong co chi tiet cuu tro de cap nhat ton kho");
		}

		Map<Long, Integer> soLuongYeuCauTheoVatPham = new LinkedHashMap<>();
		for (ChiTietCuuTroEntity chiTiet : chiTietList) {
			VatPhamEntity vatPham = chiTiet.getVatPham();
			if (vatPham == null || vatPham.getId() == null) {
				throw new InvalidFieldException("Chi tiet cuu tro co vat pham khong hop le");
			}
			Integer soLuongYeuCau = chiTiet.getSoLuong();
			if (soLuongYeuCau == null || soLuongYeuCau <= 0) {
				throw new InvalidFieldException("So luong vat pham yeu cau phai lon hon 0");
			}
			soLuongYeuCauTheoVatPham.merge(vatPham.getId(), soLuongYeuCau, Integer::sum);
		}
		return soLuongYeuCauTheoVatPham;
	}

	private boolean daTruTonKhoVatPham(TrangThaiPhieuHoTro trangThaiHienTai) {
		return trangThaiHienTai == TrangThaiPhieuHoTro.DA_NHAN
				|| trangThaiHienTai == TrangThaiPhieuHoTro.DANG_TREN_DUONG_TOI
				|| trangThaiHienTai == TrangThaiPhieuHoTro.DANG_XU_LY;
	}

	private List<TaoPhieuHoTroTepTinRequest> resolveTepTinRequests(TaoPhieuHoTroRequest request) {
		List<TaoPhieuHoTroTepTinRequest> tepTinRequests = request.getTepTins();
		if (tepTinRequests != null && !tepTinRequests.isEmpty()) {
			return tepTinRequests;
		}
		if (request.getTepTinId() == null) {
			throw new InvalidFieldException("tepTins is required");
		}
		return List.of(TaoPhieuHoTroTepTinRequest.builder()
				.tepTinId(request.getTepTinId())
				.loai(LOAI_TEP_TIN_MAC_DINH)
				.thuTu(0)
				.moTa(null)
				.build());
	}

	private List<PhieuCuuTroTepTinEntity> buildPhieuCuuTroTepTin(
			PhieuCuuTroEntity phieu,
			List<TaoPhieuHoTroTepTinRequest> tepTinRequests) {
		Set<Long> tepTinDaThem = new HashSet<>();
		List<PhieuCuuTroTepTinEntity> tepTinEntities = new ArrayList<>(tepTinRequests.size());

		for (int i = 0; i < tepTinRequests.size(); i++) {
			TaoPhieuHoTroTepTinRequest tepTinRequest = tepTinRequests.get(i);
			Long tepTinId = tepTinRequest.getTepTinId();
			if (!tepTinDaThem.add(tepTinId)) {
				throw new InvalidFieldException("Khong duoc trung tepTinId trong tepTins");
			}

			PhieuCuuTroTepTinEntity tepTinEntity = new PhieuCuuTroTepTinEntity();
			tepTinEntity.setPhieuCuuTro(phieu);
			tepTinEntity.setTepTin(tepTinRepository.findById(tepTinId)
					.orElseThrow(() -> new EntityNotFoundException("TepTin not found with id=" + tepTinId)));
			tepTinEntity.setLoai(tepTinRequest.getLoai().trim());
			tepTinEntity.setThuTu(tepTinRequest.getThuTu() == null ? i : tepTinRequest.getThuTu());
			tepTinEntity.setMoTa(StringUtils.hasText(tepTinRequest.getMoTa()) ? tepTinRequest.getMoTa().trim() : null);
			tepTinEntities.add(tepTinEntity);
		}

		return tepTinEntities;
	}

	private List<PhieuCuuTroChiTietDto> toChiTietDtos(List<ChiTietCuuTroEntity> chiTietEntities) {
		return chiTietEntities.stream()
				.map(chiTiet -> PhieuCuuTroChiTietDto.builder()
						.id(chiTiet.getId())
						.vatPhamId(chiTiet.getVatPham() != null ? chiTiet.getVatPham().getId() : null)
						.tenVatPham(chiTiet.getVatPham() != null ? chiTiet.getVatPham().getTenVatPham() : null)
						.iconUrl(chiTiet.getVatPham() != null && chiTiet.getVatPham().getTepTin() != null
								? chiTiet.getVatPham().getTepTin().getDuongDan()
								: null)
						.soLuong(chiTiet.getSoLuong())
						.ghiChu(chiTiet.getGhiChu())
						.createdAt(chiTiet.getCreatedAt())
					.build())
				.toList();
	}

	private void validateCurrentUserLaDoiTruong(Long doiNhomId) {
		NguoiDungEntity nguoiDung = getNguoiDungByTaiKhoanIdOrThrow(getCurrentTaiKhoanIdOrThrow());
		boolean laDoiTruong = doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndTinhNguyenVien_NguoiDung_IdAndVaiTro(
				doiNhomId,
				nguoiDung.getId(),
				DOI_TRUONG_VAI_TRO);
		if (!laDoiTruong) {
			throw new InvalidFieldException("Ban khong phai truong nhom cua doi duoc dieu phoi");
		}
	}

	private ResolvedNguoiGui resolveNguoiGui(NguoiGuiRequest request) {
		if (request == null || request.getType() == null) {
			throw new InvalidFieldException("nguoiGui.type is required");
		}
		return switch (request.getType()) {
			case VANG_LAI -> resolveNguoiGuiVangLai(request);
			case NGUOI_DUNG -> resolveNguoiGuiNguoiDung(request);
		};
	}

	private ResolvedNguoiGui resolveNguoiGuiVangLai(NguoiGuiRequest request) {
		if (request.getUserId() != null) {
			throw new InvalidFieldException("nguoiGui.userId must be null when nguoiGui.type = VANG_LAI");
		}
		String ten = normalizeRequired(request.getTen(), "nguoiGui.ten is required when nguoiGui.type = VANG_LAI");
		String sdt = normalizeRequired(request.getSdt(), "nguoiGui.sdt is required when nguoiGui.type = VANG_LAI");
		return new ResolvedNguoiGui(null, ten, sdt);
	}

	private ResolvedNguoiGui resolveNguoiGuiNguoiDung(NguoiGuiRequest request) {
		NguoiDungEntity nguoiDung = resolveNguoiDungByUserIdOrContext(request.getUserId());
		String ten = firstNonBlank(nguoiDung.getTen(), request.getTen());
		String sdt = firstNonBlank(nguoiDung.getSdt(), request.getSdt());
		if (!StringUtils.hasText(ten)) {
			throw new InvalidFieldException("Khong tim thay ten nguoi gui cho nguoi dung da dang ky");
		}
		if (!StringUtils.hasText(sdt)) {
			throw new InvalidFieldException("Khong tim thay sdt nguoi gui cho nguoi dung da dang ky");
		}
		return new ResolvedNguoiGui(nguoiDung, ten.trim(), sdt.trim());
	}

	private NguoiDungEntity resolveNguoiDungByUserIdOrContext(UUID userId) {
		if (userId != null) {
			return nguoiDungRepository.findById(userId)
					.orElseThrow(() -> new EntityNotFoundException("NguoiDung not found with id=" + userId));
		}
		Long taiKhoanId = getCurrentTaiKhoanId();
		if (taiKhoanId == null) {
			throw new InvalidFieldException("nguoiGui.userId is required when nguoiGui.type = NGUOI_DUNG");
		}
		return nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.orElseThrow(() -> new InvalidFieldException("Khong tim thay thong tin nguoi dung tu tai khoan dang nhap"));
	}

	private String normalizeRequired(String value, String message) {
		if (!StringUtils.hasText(value)) {
			throw new InvalidFieldException(message);
		}
		return value.trim();
	}

	private String firstNonBlank(String first, String second) {
		if (StringUtils.hasText(first)) {
			return first.trim();
		}
		if (StringUtils.hasText(second)) {
			return second.trim();
		}
		return null;
	}

	private NguoiGuiDto buildNguoiGuiDto(PhieuCuuTroEntity phieu) {
		NguoiDungEntity nguoiDung = phieu.getNguoiDung();
		String ten = StringUtils.hasText(phieu.getHoTen()) ? phieu.getHoTen() : (nguoiDung != null ? nguoiDung.getTen() : null);
		String sdt = StringUtils.hasText(phieu.getSdt()) ? phieu.getSdt() : (nguoiDung != null ? nguoiDung.getSdt() : null);
		return NguoiGuiDto.builder()
				.type(nguoiDung != null ? LoaiNguoiGui.NGUOI_DUNG : LoaiNguoiGui.VANG_LAI)
				.userId(nguoiDung != null ? nguoiDung.getId() : null)
				.ten(ten)
				.sdt(sdt)
				.build();
	}

	private record ResolvedNguoiGui(NguoiDungEntity nguoiDung, String ten, String sdt) {
	}

	private NguoiDungEntity getNguoiDungByTaiKhoanIdOrThrow(Long taiKhoanId) {
		return nguoiDungRepository.findByTaiKhoan_Id(taiKhoanId)
				.orElseThrow(() -> new InvalidFieldException("Khong tim thay thong tin nguoi dung tu tai khoan dang nhap"));
	}

	private Long getCurrentTaiKhoanIdOrThrow() {
		Long taiKhoanId = getCurrentTaiKhoanId();
		if (taiKhoanId == null) {
			throw new BadCredentialsException("Unauthorized");
		}
		return taiKhoanId;
	}

	private Long getCurrentTaiKhoanId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			return null;
		}
		Object claim = jwt.getClaim("taiKhoanId");
		if (claim == null) {
			return null;
		}
		if (claim instanceof Number number) {
			return number.longValue();
		}
		if (claim instanceof String text && StringUtils.hasText(text)) {
			return Long.parseLong(text);
		}
		return null;
	}

	private Set<String> getCurrentRoles() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getAuthorities() == null) {
			return Set.of();
		}
		return authentication.getAuthorities()
				.stream()
				.map(grantedAuthority -> grantedAuthority.getAuthority())
				.filter(StringUtils::hasText)
				.collect(Collectors.toSet());
	}

	private void validateCoQuyenChat(PhieuCuuTroEntity phieu, NguoiDungEntity sender, Set<String> roles) {
		Set<String> activeRoles = roles == null ? Set.of() : roles;
		if (activeRoles.contains(RoleType.ADMIN.name())) {
			return;
		}

		if (activeRoles.contains(RoleType.NGUOI_DAN.name())) {
			if (phieu.getNguoiDung() == null || !Objects.equals(phieu.getNguoiDung().getId(), sender.getId())) {
				throw new InvalidFieldException("Nguoi dan chi duoc chat tren phieu cua chinh minh");
			}
			return;
		}

		if (activeRoles.contains(RoleType.TRUONG_NHOM_TNV.name())) {
			PhanCongEntity phanCong = getPhanCongOrThrow(phieu.getId());
			boolean laDoiTruong = doiNhomTinhNguyenVienRepository.existsByDoiNhom_IdAndTinhNguyenVien_NguoiDung_IdAndVaiTro(
					phanCong.getDoiNhom().getId(),
					sender.getId(),
					DOI_TRUONG_VAI_TRO);
			if (!laDoiTruong) {
				throw new InvalidFieldException("Ban khong duoc phep chat tren phieu nay");
			}
			return;
		}

		throw new InvalidFieldException("Role hien tai khong duoc phep chat");
	}

	private void validateLuotTrangThai(TrangThaiPhieuHoTro hienTai, TrangThaiPhieuHoTro moi) {
		if (hienTai == TrangThaiPhieuHoTro.CHO_DIEU_PHOI) {
			throw new InvalidFieldException("Can nhan nhiem vu truoc khi cap nhat trang thai");
		}
		if (hienTai == TrangThaiPhieuHoTro.HOAN_THANH || hienTai == TrangThaiPhieuHoTro.HUY) {
			throw new InvalidFieldException("Phieu da ket thuc, khong the cap nhat trang thai");
		}
		if (moi == TrangThaiPhieuHoTro.CHO_DIEU_PHOI || moi == TrangThaiPhieuHoTro.DA_NHAN) {
			throw new InvalidFieldException("Trang thai khong hop le de cap nhat tu nghiep vu doi nhom");
		}
		if (moi == TrangThaiPhieuHoTro.HUY) {
			return;
		}
		if (hienTai == TrangThaiPhieuHoTro.DA_NHAN && moi == TrangThaiPhieuHoTro.DANG_TREN_DUONG_TOI) {
			return;
		}
		if (hienTai == TrangThaiPhieuHoTro.DANG_TREN_DUONG_TOI && moi == TrangThaiPhieuHoTro.DANG_XU_LY) {
			return;
		}
		if (hienTai == TrangThaiPhieuHoTro.DANG_XU_LY && moi == TrangThaiPhieuHoTro.HOAN_THANH) {
			return;
		}
		throw new InvalidFieldException("Khong the cap nhat trang thai tu " + hienTai.name() + " sang " + moi.name());
	}

	private TrangThaiPhieuHoTro parseTrangThai(String raw) {
		if (!StringUtils.hasText(raw)) {
			throw new InvalidFieldException("trangThai phieu khong hop le");
		}
		String normalized = raw.trim().toUpperCase();
		return switch (normalized) {
			case "PENDING" -> TrangThaiPhieuHoTro.CHO_DIEU_PHOI;
			case "ASSIGNED", "ACCEPTED" -> TrangThaiPhieuHoTro.DA_NHAN;
			case "IN_PROGRESS", "PROCESSING" -> TrangThaiPhieuHoTro.DANG_XU_LY;
			case "COMPLETED", "DONE" -> TrangThaiPhieuHoTro.HOAN_THANH;
			case "CHO_DIEU_PHOI", "DA_NHAN", "DANG_TREN_DUONG_TOI", "DANG_XU_LY", "HOAN_THANH", "HUY" ->
				TrangThaiPhieuHoTro.valueOf(normalized);
			default -> throw new InvalidFieldException(
					"trangThai must be one of: CHO_DIEU_PHOI, DA_NHAN, DANG_TREN_DUONG_TOI, DANG_XU_LY, HOAN_THANH, HUY");
		};
	}

	private TrangThaiDoiNhom resolveTrangThaiDoiNhom(DoiNhomEntity doiNhom) {
		if (!Boolean.TRUE.equals(doiNhom.getTrangThaiHoatDong()) || !Boolean.TRUE.equals(doiNhom.getActive())) {
			return TrangThaiDoiNhom.NGUNG_HOAT_DONG;
		}
		String rawStatus = doiNhom.getTrangThai();
		if (DOI_NHOM_TRANG_THAI_HOAT_DONG_DB.equalsIgnoreCase(rawStatus)) {
			return TrangThaiDoiNhom.DANG_HOAT_DONG;
		}
		return TrangThaiDoiNhom.DANG_RANH;
	}

	private void setDoiNhomDangRanh(DoiNhomEntity doiNhom) {
		doiNhom.setTrangThai(DOI_NHOM_TRANG_THAI_RANH_DB);
		doiNhomRepository.save(doiNhom);
	}

	private TrangThaiPhieuHoTro getTrangThaiNghiepVu(PhieuCuuTroEntity phieu, PhanCongEntity phanCong) {
		if (phanCong != null && StringUtils.hasText(phanCong.getTrangThai())) {
			return parseTrangThai(phanCong.getTrangThai());
		}
		return parseTrangThai(phieu.getTrangThai());
	}

	private String toDbTrangThai(TrangThaiPhieuHoTro trangThai) {
		return switch (trangThai) {
			case CHO_DIEU_PHOI -> PHIEU_TRANG_THAI_PENDING_DB;
			case DA_NHAN -> PHIEU_TRANG_THAI_ASSIGNED_DB;
			case DANG_TREN_DUONG_TOI, DANG_XU_LY -> PHIEU_TRANG_THAI_PROCESSING_DB;
			case HOAN_THANH, HUY -> PHIEU_TRANG_THAI_DONE_DB;
		};
	}

	private TrangThaiPhieuResponse buildTrangThaiResponse(PhieuCuuTroEntity phieu, PhanCongEntity phanCong) {
		return TrangThaiPhieuResponse.builder()
				.phieuId(phieu.getId())
				.trangThai(getTrangThaiNghiepVu(phieu, phanCong).name())
				.build();
	}
}
