package com.backend.cuutro.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.VatPhamEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface VatPhamRepository extends JpaRepository<VatPhamEntity, Long>, JpaSpecificationExecutor<VatPhamEntity> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select vp from VatPhamEntity vp where vp.id in :ids")
	List<VatPhamEntity> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);
}
