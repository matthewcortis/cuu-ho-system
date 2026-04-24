package com.example.cuutro.features.sos.data;

import androidx.annotation.NonNull;

import com.example.cuutro.R;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.sos.data.remote.SosApiService;
import com.example.cuutro.features.sos.data.remote.dto.PhieuCuuTroSummaryDto;
import com.example.cuutro.features.sos.model.EmergencyReportItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SosRepository {

    private static final String DEFAULT_LOCATION = "Không rõ vị trí";
    private static final String DEFAULT_TITLE = "Sự cố khẩn cấp";
    private static final String DEFAULT_DESCRIPTION = "Chưa có mô tả chi tiết";

    private final SosApiService sosApiService;
    private final AuthRepository authRepository;
    private final NetworkCallExecutor networkCallExecutor;

    public SosRepository(
            @NonNull SosApiService sosApiService,
            @NonNull AuthRepository authRepository,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.sosApiService = sosApiService;
        this.authRepository = authRepository;
        this.networkCallExecutor = networkCallExecutor;
    }

    public void getEmergencyReports(@NonNull ResultCallback<List<EmergencyReportItem>> callback) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Vui lòng đăng nhập để xem lịch sử cứu trợ"));
            return;
        }
        networkCallExecutor.execute(
                sosApiService.getEmergencyReports(),
                new ResultCallback<List<PhieuCuuTroSummaryDto>>() {
                    @Override
                    public void onSuccess(List<PhieuCuuTroSummaryDto> data) {
                        if (data == null) {
                            callback.onSuccess(Collections.emptyList());
                            return;
                        }
                        callback.onSuccess(mapEmergencyReports(data));
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    @NonNull
    private List<EmergencyReportItem> mapEmergencyReports(@NonNull List<PhieuCuuTroSummaryDto> responses) {
        List<EmergencyReportItem> reports = new ArrayList<>();
        for (PhieuCuuTroSummaryDto response : responses) {
            if (response == null) {
                continue;
            }

            String id = response.getId() == null
                    ? "local_" + reports.size()
                    : String.valueOf(response.getId());
            String title = resolveTitle(response.getLoaiSuCo());
            String location = resolveLocation(response.getViTri());
            String description = fallbackIfBlank(response.getGhiChu(), DEFAULT_DESCRIPTION);
            boolean isCompleted = isCompletedStatus(response.getTrangThai());
            int iconResId = resolveIcon(title);

            reports.add(new EmergencyReportItem(
                    id,
                    location,
                    title,
                    description,
                    iconResId,
                    isCompleted
            ));
        }
        return reports;
    }

    private String resolveTitle(PhieuCuuTroSummaryDto.LoaiSuCoDto loaiSuCo) {
        if (loaiSuCo == null) {
            return DEFAULT_TITLE;
        }
        return fallbackIfBlank(loaiSuCo.getTen(), DEFAULT_TITLE);
    }

    private String resolveLocation(PhieuCuuTroSummaryDto.ViTriDto viTri) {
        if (viTri == null) {
            return DEFAULT_LOCATION;
        }
        return fallbackIfBlank(viTri.getDiaChi(), DEFAULT_LOCATION);
    }

    private boolean isCompletedStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return "HOAN_THANH".equals(normalized)
                || "HUY".equals(normalized)
                || "DONE".equals(normalized);
    }

    private int resolveIcon(String title) {
        String normalized = title == null ? "" : title.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "accident", "tai nan", "collision", "crash")) {
            return R.drawable.ic_emergency_accident;
        }
        if (containsAny(normalized, "fire", "chay")) {
            return R.drawable.ic_emergency_fire;
        }
        if (containsAny(normalized, "medical", "y te", "cap cuu", "benh")) {
            return R.drawable.ic_emergency_medical;
        }
        if (containsAny(normalized, "flood", "ngap", "lut")) {
            return R.drawable.ic_emergency_flood;
        }
        if (containsAny(normalized, "quake", "dong dat")) {
            return R.drawable.ic_emergency_quake;
        }
        if (containsAny(normalized, "robbery", "cuop", "trom")) {
            return R.drawable.ic_emergency_robbery;
        }
        if (containsAny(normalized, "assault", "bao luc", "hanh hung")) {
            return R.drawable.ic_emergency_assault;
        }
        return R.drawable.ic_emergency_other;
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String fallbackIfBlank(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }
}
