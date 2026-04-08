package com.backend.cuutro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.PhanCongEntity;

@Repository
public interface PhanCongRepository extends JpaRepository<PhanCongEntity, Long>, JpaSpecificationExecutor<PhanCongEntity> {

	boolean existsByPhieuCuuTro_Id(Long phieuCuuTroId);

	Optional<PhanCongEntity> findByPhieuCuuTro_Id(Long phieuCuuTroId);

	boolean existsByDoiNhom_IdAndTrangThaiNotIn(Long doiNhomId, java.util.Collection<String> trangThaiKetThuc);
}
