package com.backend.cuutro.services.impl;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.backend.cuutro.dto.request.VatPhamCreateWithImageRequest;
import com.backend.cuutro.dto.request.VatPhamFilterRequest;
import com.backend.cuutro.dto.request.VatPhamUpsertRequest;
import com.backend.cuutro.dto.response.entities.DonViDto;
import com.backend.cuutro.dto.response.entities.NhomVatPhamDto;
import com.backend.cuutro.dto.response.entities.TepTinDto;
import com.backend.cuutro.dto.response.entities.VatPhamDto;
import com.backend.cuutro.entities.DonViEntity;
import com.backend.cuutro.entities.NhomVatPhamEntity;
import com.backend.cuutro.entities.TepTinEntity;
import com.backend.cuutro.entities.VatPhamEntity;
import com.backend.cuutro.mapper.VatPhamMapper;
import com.backend.cuutro.repository.DonViRepository;
import com.backend.cuutro.repository.NhomVatPhamRepository;
import com.backend.cuutro.repository.TepTinRepository;
import com.backend.cuutro.repository.VatPhamRepository;
import com.backend.cuutro.repository.specification.VatPhamSpecifications;
import com.backend.cuutro.services.FileUploadService;
import com.backend.cuutro.services.VatPhamService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VatPhamServiceImpl implements VatPhamService {

	private final VatPhamRepository vatPhamRepository;
	private final DonViRepository donViRepository;
	private final NhomVatPhamRepository nhomVatPhamRepository;
	private final TepTinRepository tepTinRepository;
	private final VatPhamMapper vatPhamMapper;
	private final FileUploadService fileUploadService;
	private final JdbcTemplate jdbcTemplate;

	@Override
	public Page<VatPhamEntity> search(VatPhamFilterRequest filter, Pageable pageable) {
		return vatPhamRepository.findAll(VatPhamSpecifications.withFilter(filter), pageable);
	}

	@Override
	public List<VatPhamEntity> search(VatPhamFilterRequest filter) {
		return vatPhamRepository.findAll(VatPhamSpecifications.withFilter(filter));
	}

	@Override
	@Transactional
	public VatPhamDto create(VatPhamUpsertRequest request) {
		VatPhamEntity entity = new VatPhamEntity();
		applyRequest(entity, request);
		return vatPhamMapper.toDto(vatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public VatPhamDto createWithImage(VatPhamCreateWithImageRequest request) {
		VatPhamEntity entity = new VatPhamEntity();
		applyBaseRequest(
				entity,
				request.getTenVatPham(),
				request.getSoLuong(),
				request.getDonViId(),
				request.getNhomVatPhamId(),
				request.getTrangThai());
		entity.setTepTin(createTepTinFromImage(request));
		return vatPhamMapper.toDto(vatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public VatPhamDto update(Long id, VatPhamUpsertRequest request) {
		VatPhamEntity entity = getEntityOrThrow(id);
		applyRequest(entity, request);
		return vatPhamMapper.toDto(vatPhamRepository.save(entity));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		VatPhamEntity entity = getEntityOrThrow(id);
		vatPhamRepository.delete(entity);
	}

	@Override
	public VatPhamDto getById(Long id) {
		return vatPhamMapper.toDto(getEntityOrThrow(id));
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
	public List<VatPhamDto> getAll() {
		try {
			return vatPhamMapper.toDtoList(vatPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
		} catch (RuntimeException ex) {
			log.warn("Fallback getAll vat_pham via JDBC due to ORM mapping error: {}", ex.getMessage());
			return getAllFallbackByJdbc();
		}
	}

	private VatPhamEntity getEntityOrThrow(Long id) {
		return vatPhamRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("VatPham not found with id=" + id));
	}

	private DonViEntity getDonViOrThrow(Long id) {
		return donViRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("DonVi not found with id=" + id));
	}

	private NhomVatPhamEntity getNhomVatPhamOrThrow(Long id) {
		return nhomVatPhamRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("NhomVatPham not found with id=" + id));
	}

	private TepTinEntity getTepTinOrThrow(Long id) {
		return tepTinRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("TepTin not found with id=" + id));
	}

	private TepTinEntity createTepTinFromImage(VatPhamCreateWithImageRequest request) {
		String imageUrl = fileUploadService.uploadFile(
				request.getAnhVatPham(),
				buildCloudinaryFolder(request.getTenVatPham()),
				buildCloudinaryPublicId(request.getTepTinId(), request.getTenVatPham()));

		TepTinEntity tepTinEntity = new TepTinEntity();
		tepTinEntity.setDuongDan(imageUrl);
		tepTinEntity.setLoaiTepTin(resolveLoaiTepTin(request.getAnhVatPham()));
		return tepTinRepository.save(tepTinEntity);
	}

	private String resolveLoaiTepTin(MultipartFile file) {
		if (file == null) {
			return "image";
		}
		if (StringUtils.hasText(file.getContentType())) {
			return file.getContentType();
		}
		if (!StringUtils.hasText(file.getOriginalFilename()) || !file.getOriginalFilename().contains(".")) {
			return "image";
		}
		return file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
	}

	private String buildCloudinaryFolder(String tenVatPham) {
		return toSlug(tenVatPham);
	}

	private String buildCloudinaryPublicId(Long tepTinId, String tenVatPham) {
		if (tepTinId != null) {
			return "tep-tin-" + tepTinId;
		}
		return toSlug(tenVatPham) + "-" + System.currentTimeMillis();
	}

	private String toSlug(String text) {
		String normalizedText = StringUtils.hasText(text) ? text.trim() : "vat-pham";
		String normalizedWithoutAccent = Normalizer.normalize(normalizedText, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		String slug = normalizedWithoutAccent.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
		return StringUtils.hasText(slug) ? slug : "vat-pham";
	}

	private void applyBaseRequest(
			VatPhamEntity entity,
			String tenVatPham,
			Short soLuong,
			Long donViId,
			Long nhomVatPhamId,
			Boolean trangThai) {
		entity.setTenVatPham(tenVatPham.trim());
		entity.setSoLuong(soLuong);
		entity.setDonVi(getDonViOrThrow(donViId));
		entity.setNhomVatPham(getNhomVatPhamOrThrow(nhomVatPhamId));
		entity.setTrangThai(trangThai == null ? Boolean.TRUE : trangThai);
	}

	private void applyRequest(VatPhamEntity entity, VatPhamUpsertRequest request) {
		applyBaseRequest(
				entity,
				request.getTenVatPham(),
				request.getSoLuong(),
				request.getDonViId(),
				request.getNhomVatPhamId(),
				request.getTrangThai());
		entity.setTepTin(request.getTepTinId() == null ? null : getTepTinOrThrow(request.getTepTinId()));
	}

	private List<VatPhamDto> getAllFallbackByJdbc() {
		String sql = """
				SELECT vp.id AS vp_id,
				       vp.ten_vat_pham AS vp_ten_vat_pham,
				       vp.so_luong AS vp_so_luong,
				       vp.trang_thai AS vp_trang_thai,
				       vp.created_at AS vp_created_at,
				       dv.id AS dv_id,
				       dv.ten AS dv_ten,
				       nvp.id AS nvp_id,
				       nvp.ten AS nvp_ten,
				       tt.id AS tt_id,
				       tt.duong_dan AS tt_duong_dan,
				       tt.loai_tep_tin AS tt_loai_tep_tin
				FROM vat_pham vp
				LEFT JOIN don_vi dv ON dv.id = vp.don_vi_id
				LEFT JOIN nhom_vat_pham nvp ON nvp.id = vp.nhom_vat_pham_id
				LEFT JOIN tep_tin tt ON tt.id = vp.tep_tin_id
				ORDER BY vp.id DESC
				""";

		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			Long donViId = rs.getObject("dv_id", Long.class);
			Long nhomVatPhamId = rs.getObject("nvp_id", Long.class);
			Long tepTinId = rs.getObject("tt_id", Long.class);

			DonViDto donVi = donViId == null
					? null
					: DonViDto.builder()
							.id(donViId)
							.ten(rs.getString("dv_ten"))
							.maDonVi(null)
							.createdAt(null)
							.build();
			NhomVatPhamDto nhomVatPham = nhomVatPhamId == null
					? null
					: NhomVatPhamDto.builder()
							.id(nhomVatPhamId)
							.ten(rs.getString("nvp_ten"))
							.moTa(null)
							.loaiSuCo(null)
							.createdAt(null)
							.build();
			TepTinDto tepTin = tepTinId == null
					? null
					: TepTinDto.builder()
							.id(tepTinId)
							.duongDan(rs.getString("tt_duong_dan"))
							.loaiTepTin(rs.getString("tt_loai_tep_tin"))
							.createdAt(null)
							.build();

			Short soLuong = rs.getObject("vp_so_luong", Short.class);
			Boolean trangThai = rs.getObject("vp_trang_thai", Boolean.class);
			Instant createdAt = null;
			java.sql.Timestamp createdAtTimestamp = rs.getTimestamp("vp_created_at");
			if (createdAtTimestamp != null) {
				createdAt = createdAtTimestamp.toInstant();
			}

			return VatPhamDto.builder()
					.id(rs.getLong("vp_id"))
					.tenVatPham(rs.getString("vp_ten_vat_pham"))
					.soLuong(soLuong)
					.donVi(donVi)
					.nhomVatPham(nhomVatPham)
					.tepTin(tepTin)
					.trangThai(trangThai)
					.createdAt(createdAt)
					.build();
		});
	}
}
