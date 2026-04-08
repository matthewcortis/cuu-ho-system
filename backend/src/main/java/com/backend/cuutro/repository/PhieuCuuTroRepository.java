package com.backend.cuutro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.PhieuCuuTroEntity;

@Repository
public interface PhieuCuuTroRepository extends JpaRepository<PhieuCuuTroEntity, Long>, JpaSpecificationExecutor<PhieuCuuTroEntity> {

	List<PhieuCuuTroEntity> findAllByOrderByCreatedAtDesc();
}
