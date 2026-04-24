package com.backend.cuutro.services;

import com.backend.cuutro.dto.response.entities.TrangThaiDangGoTinNhanResponse;
import com.backend.cuutro.dto.response.entities.TinNhanDaXemResponse;
import com.backend.cuutro.dto.response.entities.TinNhanDto;

public interface TinNhanRealtimePublisher {

	void publishTinNhan(Long phieuCuuTroId, TinNhanDto tinNhan);

	void publishTinNhanDaXem(Long phieuCuuTroId, TinNhanDaXemResponse tinNhanDaXem);

	void publishTrangThaiDangGo(Long phieuCuuTroId, TrangThaiDangGoTinNhanResponse trangThaiDangGo);
}
