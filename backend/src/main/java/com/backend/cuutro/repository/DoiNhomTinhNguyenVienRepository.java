package com.backend.cuutro.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.DoiNhomTinhNguyenVienEntity;

@Repository
public interface DoiNhomTinhNguyenVienRepository extends JpaRepository<DoiNhomTinhNguyenVienEntity, Long>, JpaSpecificationExecutor<DoiNhomTinhNguyenVienEntity> {
	boolean existsByDoiNhom_IdAndTinhNguyenVien_Id(Long doiNhomId, Long tinhNguyenVienId);

	long countByTinhNguyenVien_Id(Long tinhNguyenVienId);

	boolean existsByDoiNhom_IdAndVaiTro(Long doiNhomId, String vaiTro);

	boolean existsByTinhNguyenVien_IdAndVaiTro(Long tinhNguyenVienId, String vaiTro);

	boolean existsByDoiNhom_IdAndTinhNguyenVien_NguoiDung_IdAndVaiTro(Long doiNhomId, UUID nguoiDungId, String vaiTro);
}
