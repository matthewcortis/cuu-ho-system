package com.backend.cuutro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.TinNhanEntity;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhanEntity, Long>, JpaSpecificationExecutor<TinNhanEntity> {

	List<TinNhanEntity> findByPhieuCuuTro_IdOrderByCreatedAtAsc(Long phieuCuuTroId);
}
