package com.example.cuutro.features.sos.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.R;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.sos.data.remote.SosApiService;
import com.example.cuutro.features.sos.data.remote.dto.CapNhatTrangThaiPhieuRequestDto;
import com.example.cuutro.features.sos.data.remote.dto.PhieuCuuTroSummaryDto;
import com.example.cuutro.features.sos.data.remote.dto.TrangThaiPhieuResponseDto;
import com.example.cuutro.features.sos.model.EmergencyReportDetail;
import com.example.cuutro.features.sos.model.EmergencyReportItem;
import com.example.cuutro.features.sos.model.EmergencyReportSupplyItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SosRepository {

    private static final String DEFAULT_LOCATION = "Không rõ vị trí";
    private static final String DEFAULT_TITLE = "Sự cố khẩn cấp";
    private static final String DEFAULT_DESCRIPTION = "Chưa có mô tả chi tiết";
    private static final String DEFAULT_REPORTER_NAME = "Chưa có thông tin";
    private static final String DEFAULT_REPORTER_PHONE = "";
    private static final String DEFAULT_SUPPLY_NAME = "Vật phẩm hỗ trợ";
    private static final String DEFAULT_SUPPLY_NOTE = "Chưa có ghi chú";
    public static final String TRANG_THAI_CHO_DIEU_PHOI = "CHO_DIEU_PHOI";
    public static final String TRANG_THAI_DA_NHAN = "DA_NHAN";
    public static final String TRANG_THAI_DANG_TREN_DUONG_TOI = "DANG_TREN_DUONG_TOI";
    public static final String TRANG_THAI_DANG_XU_LY = "DANG_XU_LY";
    public static final String TRANG_THAI_HOAN_THANH = "HOAN_THANH";
    public static final String TRANG_THAI_HUY = "HUY";

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

    public void getEmergencyReportDetail(
            @NonNull String reportId,
            @NonNull ResultCallback<EmergencyReportDetail> callback
    ) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Vui lòng đăng nhập để xem chi tiết phiếu cứu trợ"));
            return;
        }

        Long parsedReportId = parseReportId(reportId);
        if (parsedReportId == null) {
            callback.onError(new NetworkError(NetworkError.CODE_UNKNOWN, "Mã phiếu không hợp lệ"));
            return;
        }

        networkCallExecutor.execute(
                sosApiService.getEmergencyReportById(parsedReportId),
                new ResultCallback<PhieuCuuTroSummaryDto>() {
                    @Override
                    public void onSuccess(PhieuCuuTroSummaryDto data) {
                        if (data == null) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    "Không tìm thấy chi tiết phiếu cứu trợ"
                            ));
                            return;
                        }
                        callback.onSuccess(mapEmergencyReportDetail(data));
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

    public void nhanNhiemVu(
            long reportId,
            @NonNull ResultCallback<String> callback
    ) {
        executeTaskStatusCall(reportId, callback, ActionType.NHAN_NHIEM_VU, null);
    }

    public void tuChoiNhiemVu(
            long reportId,
            @NonNull ResultCallback<String> callback
    ) {
        executeTaskStatusCall(reportId, callback, ActionType.TU_CHOI_NHIEM_VU, null);
    }

    public void capNhatTrangThaiNhiemVu(
            long reportId,
            @NonNull String trangThai,
            @NonNull ResultCallback<String> callback
    ) {
        executeTaskStatusCall(reportId, callback, ActionType.CAP_NHAT_TRANG_THAI, trangThai);
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
            String status = trimToNull(response.getTrangThai());
            boolean isCompleted = isCompletedStatus(status);
            String statusLabel = resolveStatusLabel(status);
            int iconResId = resolveIcon(title);

            reports.add(new EmergencyReportItem(
                    id,
                    location,
                    title,
                    description,
                    iconResId,
                    isCompleted,
                    status,
                    statusLabel
            ));
        }
        return reports;
    }

    @NonNull
    private EmergencyReportDetail mapEmergencyReportDetail(@NonNull PhieuCuuTroSummaryDto response) {
        String id = response.getId() == null ? "N/A" : String.valueOf(response.getId());
        String title = resolveTitle(response.getLoaiSuCo());
        String location = resolveLocation(response.getViTri());
        String description = fallbackIfBlank(response.getGhiChu(), DEFAULT_DESCRIPTION);
        String status = trimToNull(response.getTrangThai());
        boolean completed = isCompletedStatus(status);

        String reporterName = DEFAULT_REPORTER_NAME;
        String reporterPhone = DEFAULT_REPORTER_PHONE;
        if (response.getNguoiGui() != null) {
            reporterName = fallbackIfBlank(response.getNguoiGui().getTen(), DEFAULT_REPORTER_NAME);
            reporterPhone = fallbackIfBlank(response.getNguoiGui().getSdt(), DEFAULT_REPORTER_PHONE);
        }

        return new EmergencyReportDetail(
                id,
                title,
                location,
                description,
                status,
                resolveStatusLabel(status),
                completed,
                reporterName,
                reporterPhone,
                trimToNull(response.getCreatedAt()),
                resolveIncidentIconUrl(response.getLoaiSuCo()),
                resolveImageUrls(response.getTepTins()),
                resolveSupplyItems(response.getChiTietCuuTro())
        );
    }

    @Nullable
    private Long parseReportId(@Nullable String reportId) {
        String normalized = trimToNull(reportId);
        if (normalized == null) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ignored) {
            return null;
        }
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

    @Nullable
    private String resolveIncidentIconUrl(@Nullable PhieuCuuTroSummaryDto.LoaiSuCoDto loaiSuCo) {
        if (loaiSuCo == null) {
            return null;
        }
        return trimToNull(loaiSuCo.getIconUrl());
    }

    public boolean isCompletedStatus(@Nullable String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return "HOAN_THANH".equals(normalized)
                || "HUY".equals(normalized)
                || "DONE".equals(normalized);
    }

    public boolean isChoDieuPhoiStatus(@Nullable String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        return TRANG_THAI_CHO_DIEU_PHOI.equals(normalized);
    }

    @NonNull
    public String resolveStatusLabel(@Nullable String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Đang xử lý";
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "CHO_DIEU_PHOI":
                return "Chờ điều phối";
            case "DA_NHAN":
                return "Đã nhận nhiệm vụ";
            case "DANG_TREN_DUONG_TOI":
                return "Đang trên đường tới";
            case "DANG_XU_LY":
                return "Đang xử lý tại hiện trường";
            case "HOAN_THANH":
                return "Hoàn thành";
            case "HUY":
                return "Đã huỷ";
            default:
                return "Đang xử lý";
        }
    }

    @NonNull
    private List<String> resolveImageUrls(@Nullable List<PhieuCuuTroSummaryDto.PhieuCuuTroTepTinDto> tepTins) {
        if (tepTins == null || tepTins.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> imageUrls = new ArrayList<>();
        for (PhieuCuuTroSummaryDto.PhieuCuuTroTepTinDto item : tepTins) {
            if (item == null || item.getTepTin() == null) {
                continue;
            }
            String url = trimToNull(item.getTepTin().getDuongDan());
            if (url == null) {
                continue;
            }
            String fileType = trimToNull(item.getTepTin().getLoaiTepTin());
            if (isImageAttachment(fileType, url)) {
                imageUrls.add(url);
            }
        }
        return imageUrls;
    }

    private boolean isImageAttachment(@Nullable String fileType, @NonNull String url) {
        String normalizedType = fileType == null ? "" : fileType.trim().toLowerCase(Locale.ROOT);
        if (containsAny(normalizedType, "image", "img", "anh", "photo", "picture")) {
            return true;
        }
        String normalizedUrl = url.toLowerCase(Locale.ROOT);
        return normalizedUrl.endsWith(".jpg")
                || normalizedUrl.endsWith(".jpeg")
                || normalizedUrl.endsWith(".png")
                || normalizedUrl.endsWith(".webp")
                || normalizedUrl.endsWith(".gif");
    }

    @NonNull
    private List<EmergencyReportSupplyItem> resolveSupplyItems(
            @Nullable List<PhieuCuuTroSummaryDto.PhieuCuuTroChiTietDto> chiTietCuuTroList
    ) {
        if (chiTietCuuTroList == null || chiTietCuuTroList.isEmpty()) {
            return Collections.emptyList();
        }

        List<EmergencyReportSupplyItem> supplyItems = new ArrayList<>();
        for (PhieuCuuTroSummaryDto.PhieuCuuTroChiTietDto item : chiTietCuuTroList) {
            if (item == null) {
                continue;
            }

            String supplyName = fallbackIfBlank(item.getTenVatPham(), DEFAULT_SUPPLY_NAME);
            String supplyNote = fallbackIfBlank(item.getGhiChu(), DEFAULT_SUPPLY_NOTE);
            supplyItems.add(new EmergencyReportSupplyItem(
                    supplyName,
                    item.getSoLuong(),
                    supplyNote,
                    trimToNull(item.getIconUrl())
            ));
        }
        return supplyItems;
    }

    private void executeTaskStatusCall(
            long reportId,
            @NonNull ResultCallback<String> callback,
            @NonNull ActionType actionType,
            @Nullable String trangThai
    ) {
        if (reportId <= 0) {
            callback.onError(new NetworkError(400, "Mã phiếu không hợp lệ"));
            return;
        }
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Vui lòng đăng nhập để thao tác nhiệm vụ"));
            return;
        }

        if (actionType == ActionType.NHAN_NHIEM_VU) {
            networkCallExecutor.execute(
                    sosApiService.nhanNhiemVu(reportId),
                    new ResultCallback<TrangThaiPhieuResponseDto>() {
                        @Override
                        public void onSuccess(TrangThaiPhieuResponseDto data) {
                            handleTaskStatusSuccess(data, callback);
                        }

                        @Override
                        public void onError(@NonNull NetworkError error) {
                            handleTaskStatusError(callback, error);
                        }
                    }
            );
            return;
        }

        if (actionType == ActionType.TU_CHOI_NHIEM_VU) {
            networkCallExecutor.execute(
                    sosApiService.tuChoiNhiemVu(reportId),
                    new ResultCallback<TrangThaiPhieuResponseDto>() {
                        @Override
                        public void onSuccess(TrangThaiPhieuResponseDto data) {
                            handleTaskStatusSuccess(data, callback);
                        }

                        @Override
                        public void onError(@NonNull NetworkError error) {
                            handleTaskStatusError(callback, error);
                        }
                    }
            );
            return;
        }

        if (actionType == ActionType.CAP_NHAT_TRANG_THAI) {
            String normalizedTrangThai = trimToNull(trangThai);
            if (normalizedTrangThai == null) {
                callback.onError(new NetworkError(400, "Trạng thái cập nhật không hợp lệ"));
                return;
            }
            networkCallExecutor.execute(
                    sosApiService.capNhatTrangThaiPhieu(
                            reportId,
                            new CapNhatTrangThaiPhieuRequestDto(normalizedTrangThai)
                    ),
                    new ResultCallback<TrangThaiPhieuResponseDto>() {
                        @Override
                        public void onSuccess(TrangThaiPhieuResponseDto data) {
                            handleTaskStatusSuccess(data, callback);
                        }

                        @Override
                        public void onError(@NonNull NetworkError error) {
                            handleTaskStatusError(callback, error);
                        }
                    }
            );
        }
    }

    private void handleTaskStatusSuccess(
            @Nullable TrangThaiPhieuResponseDto data,
            @NonNull ResultCallback<String> callback
    ) {
        if (data == null) {
            callback.onError(new NetworkError(NetworkError.CODE_UNKNOWN, "Phản hồi cập nhật nhiệm vụ không hợp lệ"));
            return;
        }
        String status = trimToNull(data.getTrangThai());
        if (status == null) {
            callback.onError(new NetworkError(NetworkError.CODE_UNKNOWN, "Thiếu trạng thái nhiệm vụ sau khi cập nhật"));
            return;
        }
        callback.onSuccess(status);
    }

    private void handleTaskStatusError(
            @NonNull ResultCallback<String> callback,
            @NonNull NetworkError error
    ) {
        if (error.isUnauthorized()) {
            authRepository.clearSession();
        }
        callback.onError(error);
    }

    private enum ActionType {
        NHAN_NHIEM_VU,
        TU_CHOI_NHIEM_VU,
        CAP_NHAT_TRANG_THAI
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

    private String fallbackIfBlank(@Nullable String value, @NonNull String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
