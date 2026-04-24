package com.backend.cuutro.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.cuutro.entities.TinNhanDaXemEntity;

@Repository
public interface TinNhanDaXemRepository extends JpaRepository<TinNhanDaXemEntity, Long>, JpaSpecificationExecutor<TinNhanDaXemEntity> {

	Optional<TinNhanDaXemEntity> findByPhieuCuuTro_IdAndNguoiDung_Id(Long phieuCuuTroId, UUID nguoiDungId);

	@Query("""
			select count(tn)
			from TinNhanEntity tn
			where tn.phieuCuuTro.id = :phieuCuuTroId
				and tn.sender.id <> :nguoiDungId
				and (:lastSeenMessageId is null or tn.id > :lastSeenMessageId)
			""")
	long countTinNhanChuaDoc(
			@Param("phieuCuuTroId") Long phieuCuuTroId,
			@Param("nguoiDungId") UUID nguoiDungId,
			@Param("lastSeenMessageId") Long lastSeenMessageId);
}

