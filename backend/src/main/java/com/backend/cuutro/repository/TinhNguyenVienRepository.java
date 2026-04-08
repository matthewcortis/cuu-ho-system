package com.backend.cuutro.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.TinhNguyenVienEntity;

@Repository
public interface TinhNguyenVienRepository extends JpaRepository<TinhNguyenVienEntity, Long>, JpaSpecificationExecutor<TinhNguyenVienEntity> {
	List<TinhNguyenVienEntity> findByTrangThaiDuyetOrderByCreatedAtDesc(String trangThaiDuyet);

	List<TinhNguyenVienEntity> findAllByOrderByCreatedAtDesc();

	boolean existsByNguoiDung_IdAndTrangThaiDuyetIn(UUID nguoiDungId, Collection<String> trangThaiDuyet);

	Optional<TinhNguyenVienEntity> findByNguoiDung_Id(UUID nguoiDungId);
}
