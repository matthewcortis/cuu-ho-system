package com.backend.cuutro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.TaiKhoanEntity;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoanEntity, Long>, JpaSpecificationExecutor<TaiKhoanEntity> {

	Optional<TaiKhoanEntity> findByTenDangNhapIgnoreCaseOrEmailIgnoreCase(String tenDangNhap, String email);
}
