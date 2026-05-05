package com.backend.cuutro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;

@Repository
public interface DoiNhomTinhNguyenVienRepository extends JpaRepository<DoiNhomTinhNguyenVienEntity, Long>, JpaSpecificationExecutor<DoiNhomTinhNguyenVienEntity> {
	List<DoiNhomTinhNguyenVienEntity> findByDoiNhom_IdInOrderByCreatedAtAsc(List<Long> doiNhomIds);

	Optional<DoiNhomTinhNguyenVienEntity> findFirstByDoiNhom_IdAndVaiTro(Long doiNhomId, String vaiTro);

	Optional<DoiNhomTinhNguyenVienEntity> findByDoiNhom_IdAndTinhNguyenVien_Id(Long doiNhomId, Long tinhNguyenVienId);

	boolean existsByDoiNhom_IdAndTinhNguyenVien_Id(Long doiNhomId, Long tinhNguyenVienId);

	boolean existsByDoiNhom_IdNotAndTinhNguyenVien_IdAndVaiTro(Long doiNhomId, Long tinhNguyenVienId, String vaiTro);

	long countByTinhNguyenVien_Id(Long tinhNguyenVienId);

	boolean existsByDoiNhom_IdAndVaiTro(Long doiNhomId, String vaiTro);

	boolean existsByTinhNguyenVien_IdAndVaiTro(Long tinhNguyenVienId, String vaiTro);

	boolean existsByTinhNguyenVien_IdAndDoiNhom_IdNotAndVaiTro(Long tinhNguyenVienId, Long doiNhomId, String vaiTro);

	boolean existsByDoiNhom_IdAndTinhNguyenVien_NguoiDung_IdAndVaiTro(Long doiNhomId, UUID nguoiDungId, String vaiTro);

	List<DoiNhomTinhNguyenVienEntity> findByTinhNguyenVien_NguoiDung_IdAndVaiTro(UUID nguoiDungId, String vaiTro);
}
