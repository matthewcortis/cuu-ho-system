package com.backend.cuutro.services.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.backend.cuutro.dto.response.entities.TrangThaiDangGoTinNhanResponse;
import com.backend.cuutro.dto.response.entities.TinNhanDaXemResponse;
import com.backend.cuutro.dto.response.entities.TinNhanDto;
import com.backend.cuutro.services.TinNhanRealtimePublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TinNhanRealtimePublisherImpl implements TinNhanRealtimePublisher {

	private static final String TOPIC_PREFIX = "/topic/phieu-cuu-tro/";
	private static final String TIN_NHAN_TOPIC = "/tin-nhan";
	private static final String TIN_NHAN_DA_XEM_TOPIC = "/tin-nhan/da-xem";
	private static final String TIN_NHAN_DANG_GO_TOPIC = "/tin-nhan/dang-go";

	private final SimpMessagingTemplate simpMessagingTemplate;

	@Override
	public void publishTinNhan(Long phieuCuuTroId, TinNhanDto tinNhan) {
		if (phieuCuuTroId == null || tinNhan == null) {
			return;
		}
		simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + phieuCuuTroId + TIN_NHAN_TOPIC, tinNhan);
	}

	@Override
	public void publishTinNhanDaXem(Long phieuCuuTroId, TinNhanDaXemResponse tinNhanDaXem) {
		if (phieuCuuTroId == null || tinNhanDaXem == null) {
			return;
		}
		simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + phieuCuuTroId + TIN_NHAN_DA_XEM_TOPIC, tinNhanDaXem);
	}

	@Override
	public void publishTrangThaiDangGo(Long phieuCuuTroId, TrangThaiDangGoTinNhanResponse trangThaiDangGo) {
		if (phieuCuuTroId == null || trangThaiDangGo == null) {
			return;
		}
		simpMessagingTemplate.convertAndSend(TOPIC_PREFIX + phieuCuuTroId + TIN_NHAN_DANG_GO_TOPIC, trangThaiDangGo);
	}
}
