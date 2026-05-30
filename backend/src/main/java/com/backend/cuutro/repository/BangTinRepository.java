package com.backend.cuutro.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.BangTinEntity;

@Repository
public interface BangTinRepository extends JpaRepository<BangTinEntity, Long>, JpaSpecificationExecutor<BangTinEntity> {

	List<BangTinEntity> findAllByOrderByCreatedAtDesc();

	List<BangTinEntity> findByTrangThaiTrueOrderByCreatedAtDesc();

	Optional<BangTinEntity> findByIdAndTrangThaiTrue(Long id);
}
