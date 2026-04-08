package com.backend.cuutro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.ChiTietCuuTroEntity;

@Repository
public interface ChiTietCuuTroRepository extends JpaRepository<ChiTietCuuTroEntity, Long>, JpaSpecificationExecutor<ChiTietCuuTroEntity> {

	List<ChiTietCuuTroEntity> findByPhieuCuuTro_IdOrderByIdAsc(Long phieuCuuTroId);
}
