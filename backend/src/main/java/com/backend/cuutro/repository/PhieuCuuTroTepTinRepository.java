package com.backend.cuutro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.PhieuCuuTroTepTinEntity;

@Repository
public interface PhieuCuuTroTepTinRepository extends JpaRepository<PhieuCuuTroTepTinEntity, Long> {

	List<PhieuCuuTroTepTinEntity> findByPhieuCuuTro_IdOrderByThuTuAscIdAsc(Long phieuCuuTroId);
}
