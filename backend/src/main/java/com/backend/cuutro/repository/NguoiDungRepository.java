package com.backend.cuutro.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.NguoiDungEntity;

@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDungEntity, UUID>, JpaSpecificationExecutor<NguoiDungEntity> {

	Optional<NguoiDungEntity> findByTaiKhoan_Id(Long taiKhoanId);
}
