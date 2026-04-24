package com.example.cuutro.features.report.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.R;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.report.data.model.ReportEmergencyType;
import com.example.cuutro.features.report.data.model.ReportSupplyItem;
import com.example.cuutro.features.report.data.remote.ReportApiService;
import com.example.cuutro.features.report.data.remote.dto.LoaiSuCoFilterPageDto;
import com.example.cuutro.features.report.data.remote.dto.LoaiSuCoFilterRequestDto;
import com.example.cuutro.features.report.data.remote.dto.LoaiSuCoItemDto;
import com.example.cuutro.features.report.data.remote.dto.NhomVatPhamItemDto;
import com.example.cuutro.features.report.data.remote.dto.TaoPhieuCuuTroRequestDto;
import com.example.cuutro.features.report.data.remote.dto.TaoPhieuCuuTroResponseDto;
import com.example.cuutro.features.report.data.remote.dto.TepTinUploadResponseDto;
import com.example.cuutro.features.report.data.remote.dto.VatPhamItemDto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ReportRepository {

    private static final int DEFAULT_FILTER_PAGE = 0;
    private static final int DEFAULT_FILTER_SIZE = 6;
    private static final String DEFAULT_LABEL = "Sự cố khác";
    private static final String DEFAULT_GROUP_NAME = "Nhóm vật phẩm";
    private static final String DEFAULT_SUPPLY_NAME = "Vật phẩm";

    private static final String REPORTER_TYPE_GUEST = "VANG_LAI";
    private static final String REPORTER_TYPE_USER = "NGUOI_DUNG";

    private static final String ATTACHMENT_TYPE_IMAGE = "image";
    private static final String ATTACHMENT_TYPE_VIDEO = "video";
    private static final String ATTACHMENT_TYPE_AUDIO = "audio";

    private static final String UPLOAD_FOLDER_REPORT = "phieu-cuu-tro";
    private static final String DEFAULT_UPLOAD_FILE_NAME = "tep-tin";

    private final Context appContext;
    private final ReportApiService reportApiService;
    private final AuthRepository authRepository;
    private final NetworkCallExecutor networkCallExecutor;
    private final List<VatPhamItemDto> cachedVatPham = new ArrayList<>();
    private final Map<Long, List<Long>> nhomVatPhamLoaiSuCoMap = new LinkedHashMap<>();
    private boolean vatPhamLoaded;
    private boolean nhomVatPhamLoaded;

    public ReportRepository(
            @NonNull Context context,
            @NonNull ReportApiService reportApiService,
            @NonNull AuthRepository authRepository,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.appContext = context.getApplicationContext();
        this.reportApiService = reportApiService;
        this.authRepository = authRepository;
        this.networkCallExecutor = networkCallExecutor;
    }

    public void getActiveEmergencyTypes(@NonNull ResultCallback<List<ReportEmergencyType>> callback) {
        getActiveEmergencyTypes(callback, true);
    }

    public void getSuppliesByEmergencyType(
            Long loaiSuCoId,
            @NonNull ResultCallback<List<ReportSupplyItem>> callback
    ) {
        if (loaiSuCoId == null || loaiSuCoId <= 0) {
            callback.onSuccess(Collections.emptyList());
            return;
        }

        if (vatPhamLoaded && nhomVatPhamLoaded) {
            callback.onSuccess(filterSuppliesByLoaiSuCoId(loaiSuCoId, cachedVatPham));
            return;
        }

        loadNhomVatPhamAndVatPham(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                callback.onSuccess(filterSuppliesByLoaiSuCoId(loaiSuCoId, cachedVatPham));
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                callback.onError(error);
            }
        }, true);
    }

    public void createRescueTicket(
            @NonNull CreateRescueTicketInput input,
            @NonNull ResultCallback<Long> callback
    ) {
        NetworkError validationError = validateCreateInput(input);
        if (validationError != null) {
            callback.onError(validationError);
            return;
        }

        List<AttachmentUploadTask> uploadTasks = buildAttachmentUploadTasks(input.getAttachments());
        if (uploadTasks.isEmpty()) {
            callback.onError(new NetworkError(
                    400,
                    appContext.getString(R.string.report_attachment_required)
            ));
            return;
        }

        uploadAttachmentsSequentially(
                input,
                uploadTasks,
                0,
                new ArrayList<>(),
                callback,
                true
        );
    }

    private void getActiveEmergencyTypes(
            @NonNull ResultCallback<List<ReportEmergencyType>> callback,
            boolean canRetryUnauthorized
    ) {
        LoaiSuCoFilterRequestDto request = buildFilterRequest();
        networkCallExecutor.execute(
                reportApiService.filterLoaiSuCo(request),
                new ResultCallback<LoaiSuCoFilterPageDto>() {
                    @Override
                    public void onSuccess(LoaiSuCoFilterPageDto data) {
                        if (data == null || data.getContent() == null) {
                            callback.onSuccess(Collections.emptyList());
                            return;
                        }
                        callback.onSuccess(mapEmergencyTypes(data.getContent()));
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized() && canRetryUnauthorized) {
                            authRepository.clearSession();
                            getActiveEmergencyTypes(callback, false);
                            return;
                        }
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    private void loadNhomVatPhamAndVatPham(
            @NonNull ResultCallback<Void> callback,
            boolean canRetryUnauthorized
    ) {
        if (nhomVatPhamLoaded) {
            loadVatPham(callback, canRetryUnauthorized);
            return;
        }

        networkCallExecutor.execute(
                reportApiService.getNhomVatPhamList(),
                new ResultCallback<List<NhomVatPhamItemDto>>() {
                    @Override
                    public void onSuccess(List<NhomVatPhamItemDto> data) {
                        syncNhomVatPhamLoaiSuCoMap(data);
                        nhomVatPhamLoaded = true;
                        loadVatPham(callback, canRetryUnauthorized);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized() && canRetryUnauthorized) {
                            authRepository.clearSession();
                            loadNhomVatPhamAndVatPham(callback, false);
                            return;
                        }
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    private void loadVatPham(@NonNull ResultCallback<Void> callback, boolean canRetryUnauthorized) {
        if (vatPhamLoaded) {
            callback.onSuccess(null);
            return;
        }

        networkCallExecutor.execute(
                reportApiService.getVatPhamList(),
                new ResultCallback<List<VatPhamItemDto>>() {
                    @Override
                    public void onSuccess(List<VatPhamItemDto> data) {
                        cachedVatPham.clear();
                        if (data != null) {
                            cachedVatPham.addAll(data);
                        }
                        vatPhamLoaded = true;
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized() && canRetryUnauthorized) {
                            authRepository.clearSession();
                            loadVatPham(callback, false);
                            return;
                        }
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    private void uploadAttachmentsSequentially(
            @NonNull CreateRescueTicketInput input,
            @NonNull List<AttachmentUploadTask> tasks,
            int index,
            @NonNull List<TaoPhieuCuuTroRequestDto.TepTinDto> uploadedAttachments,
            @NonNull ResultCallback<Long> callback,
            boolean canRetryUnauthorized
    ) {
        if (index >= tasks.size()) {
            submitCreateRescueTicket(input, uploadedAttachments, callback, canRetryUnauthorized);
            return;
        }

        AttachmentUploadTask task = tasks.get(index);
        MultipartBody.Part filePart;
        try {
            filePart = createAttachmentMultipartPart(task);
        } catch (IOException exception) {
            callback.onError(new NetworkError(
                    NetworkError.CODE_UNKNOWN,
                    appContext.getString(R.string.report_attachment_file_read_failed)
            ));
            return;
        }

        RequestBody folderBody = createPlainTextRequestBody(task.getThuMuc());
        RequestBody fileNameBody = createPlainTextRequestBody(task.getTenTep());

        networkCallExecutor.execute(
                reportApiService.uploadAttachment(filePart, folderBody, fileNameBody),
                new ResultCallback<TepTinUploadResponseDto>() {
                    @Override
                    public void onSuccess(TepTinUploadResponseDto data) {
                        Long tepTinId = data != null ? data.getId() : null;
                        if (tepTinId == null || tepTinId <= 0) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    appContext.getString(R.string.report_attachment_upload_invalid_response)
                            ));
                            return;
                        }

                        uploadedAttachments.add(new TaoPhieuCuuTroRequestDto.TepTinDto(
                                tepTinId,
                                task.getLoai(),
                                task.getThuTu()
                        ));

                        uploadAttachmentsSequentially(
                                input,
                                tasks,
                                index + 1,
                                uploadedAttachments,
                                callback,
                                canRetryUnauthorized
                        );
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized() && canRetryUnauthorized) {
                            authRepository.clearSession();
                            uploadAttachmentsSequentially(
                                    input,
                                    tasks,
                                    index,
                                    uploadedAttachments,
                                    callback,
                                    false
                            );
                            return;
                        }
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    private void submitCreateRescueTicket(
            @NonNull CreateRescueTicketInput input,
            @NonNull List<TaoPhieuCuuTroRequestDto.TepTinDto> uploadedAttachments,
            @NonNull ResultCallback<Long> callback,
            boolean canRetryUnauthorized
    ) {
        TaoPhieuCuuTroRequestDto requestBody = buildCreateRescueTicketRequest(input, uploadedAttachments);
        networkCallExecutor.execute(
                reportApiService.createRescueTicket(requestBody),
                new ResultCallback<TaoPhieuCuuTroResponseDto>() {
                    @Override
                    public void onSuccess(TaoPhieuCuuTroResponseDto data) {
                        callback.onSuccess(data != null ? data.getId() : null);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized() && canRetryUnauthorized) {
                            authRepository.clearSession();
                            submitCreateRescueTicket(input, uploadedAttachments, callback, false);
                            return;
                        }
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    @NonNull
    private TaoPhieuCuuTroRequestDto buildCreateRescueTicketRequest(
            @NonNull CreateRescueTicketInput input,
            @NonNull List<TaoPhieuCuuTroRequestDto.TepTinDto> uploadedAttachments
    ) {
        String reporterType = authRepository.hasActiveSession() ? REPORTER_TYPE_USER : REPORTER_TYPE_GUEST;
        TaoPhieuCuuTroRequestDto.NguoiGuiDto nguoiGui = new TaoPhieuCuuTroRequestDto.NguoiGuiDto(
                reporterType,
                trimToNull(input.getReporterName()),
                trimToNull(input.getReporterPhone())
        );

        TaoPhieuCuuTroRequestDto.ViTriDto viTri = new TaoPhieuCuuTroRequestDto.ViTriDto(
                trimToNull(input.getAddress()),
                trimToNull(input.getLat()),
                trimToNull(input.getLongitude())
        );

        return new TaoPhieuCuuTroRequestDto(
                input.getLoaiSuCoId(),
                viTri,
                nguoiGui,
                trimToNull(input.getGhiChu()),
                mapChiTietCuuTro(input.getSelectedSupplies()),
                uploadedAttachments
        );
    }

    @NonNull
    private List<TaoPhieuCuuTroRequestDto.ChiTietCuuTroDto> mapChiTietCuuTro(
            @Nullable List<SelectedSupplyInput> selectedSupplies
    ) {
        if (selectedSupplies == null || selectedSupplies.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashMap<Long, Integer> uniqueItems = new LinkedHashMap<>();
        for (SelectedSupplyInput selectedSupply : selectedSupplies) {
            if (selectedSupply == null || selectedSupply.getVatPhamId() == null || selectedSupply.getVatPhamId() <= 0) {
                continue;
            }
            int quantity = Math.max(1, selectedSupply.getQuantity());
            int mergedQuantity = uniqueItems.getOrDefault(selectedSupply.getVatPhamId(), 0) + quantity;
            uniqueItems.put(selectedSupply.getVatPhamId(), mergedQuantity);
        }

        List<TaoPhieuCuuTroRequestDto.ChiTietCuuTroDto> mapped = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : uniqueItems.entrySet()) {
            mapped.add(new TaoPhieuCuuTroRequestDto.ChiTietCuuTroDto(entry.getKey(), entry.getValue()));
        }
        return mapped;
    }

    @NonNull
    private List<AttachmentUploadTask> buildAttachmentUploadTasks(
            @Nullable List<AttachmentInput> attachments
    ) {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }

        List<AttachmentUploadTask> tasks = new ArrayList<>();
        int thuTu = 0;
        for (AttachmentInput attachment : attachments) {
            if (attachment == null || attachment.getUri() == null) {
                continue;
            }
            String loai = normalizeAttachmentType(attachment.getType());
            String fileName = resolveAttachmentName(attachment.getUri(), DEFAULT_UPLOAD_FILE_NAME + "-" + (thuTu + 1));
            tasks.add(new AttachmentUploadTask(
                    attachment.getUri(),
                    UPLOAD_FOLDER_REPORT,
                    fileName,
                    loai,
                    thuTu
            ));
            thuTu++;
        }
        return tasks;
    }

    @NonNull
    private String normalizeAttachmentType(@Nullable String type) {
        String normalized = trimToNull(type);
        if (normalized == null) {
            return ATTACHMENT_TYPE_IMAGE;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        if (ATTACHMENT_TYPE_VIDEO.equals(lower)) {
            return ATTACHMENT_TYPE_VIDEO;
        }
        if (ATTACHMENT_TYPE_AUDIO.equals(lower)) {
            return ATTACHMENT_TYPE_AUDIO;
        }
        return ATTACHMENT_TYPE_IMAGE;
    }

    @NonNull
    private MultipartBody.Part createAttachmentMultipartPart(@NonNull AttachmentUploadTask task) throws IOException {
        ContentResolver resolver = appContext.getContentResolver();
        String mimeType = trimToNull(resolver.getType(task.getUri()));
        MediaType mediaType = MediaType.parse(mimeType != null ? mimeType : "application/octet-stream");
        RequestBody requestBody = new UriRequestBody(resolver, task.getUri(), mediaType);
        return MultipartBody.Part.createFormData("tepTin", task.getTenTep(), requestBody);
    }

    @NonNull
    private RequestBody createPlainTextRequestBody(@Nullable String value) {
        String safeValue = value == null ? "" : value;
        return new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MultipartBody.FORM;
            }

            @Override
            public long contentLength() {
                return safeValue.getBytes(StandardCharsets.UTF_8).length;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                sink.writeUtf8(safeValue);
            }
        };
    }

    @Nullable
    private NetworkError validateCreateInput(@Nullable CreateRescueTicketInput input) {
        if (input == null) {
            return new NetworkError(400, appContext.getString(R.string.report_submit_invalid_payload));
        }

        if (input.getLoaiSuCoId() == null || input.getLoaiSuCoId() <= 0) {
            return new NetworkError(400, appContext.getString(R.string.report_select_type_first));
        }

        if (trimToNull(input.getReporterName()) == null) {
            return new NetworkError(400, appContext.getString(R.string.report_reporter_name_required));
        }

        if (trimToNull(input.getReporterPhone()) == null) {
            return new NetworkError(400, appContext.getString(R.string.report_reporter_phone_required));
        }

        if (trimToNull(input.getAddress()) == null) {
            return new NetworkError(400, appContext.getString(R.string.report_location_required));
        }

        if (trimToNull(input.getGhiChu()) == null) {
            return new NetworkError(400, appContext.getString(R.string.report_brief_required));
        }

        if (input.getSelectedSupplies() == null || input.getSelectedSupplies().isEmpty()) {
            return new NetworkError(400, appContext.getString(R.string.report_supply_select_at_least_one));
        }

        if (input.getAttachments() == null || input.getAttachments().isEmpty()) {
            return new NetworkError(400, appContext.getString(R.string.report_attachment_required));
        }

        return null;
    }

    @NonNull
    private String resolveAttachmentName(@NonNull Uri uri, @NonNull String fallback) {
        Cursor cursor = null;
        try {
            cursor = appContext.getContentResolver().query(
                    uri,
                    new String[] {OpenableColumns.DISPLAY_NAME},
                    null,
                    null,
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = trimToNull(cursor.getString(index));
                    if (name != null) {
                        return name;
                    }
                }
            }
        } catch (SecurityException ignored) {
            // Ignore and fallback.
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String lastPath = trimToNull(uri.getLastPathSegment());
        if (lastPath != null) {
            return lastPath;
        }
        return fallback;
    }

    private void syncNhomVatPhamLoaiSuCoMap(List<NhomVatPhamItemDto> nhomVatPhamItems) {
        nhomVatPhamLoaiSuCoMap.clear();
        if (nhomVatPhamItems == null) {
            return;
        }

        for (NhomVatPhamItemDto nhomVatPham : nhomVatPhamItems) {
            if (nhomVatPham == null || nhomVatPham.getId() == null) {
                continue;
            }

            LinkedHashMap<Long, Boolean> idSet = new LinkedHashMap<>();
            if (nhomVatPham.getLoaiSuCos() != null) {
                for (NhomVatPhamItemDto.LoaiSuCoDto loaiSuCo : nhomVatPham.getLoaiSuCos()) {
                    if (loaiSuCo == null || loaiSuCo.getId() == null) {
                        continue;
                    }
                    idSet.put(loaiSuCo.getId(), Boolean.TRUE);
                }
            }
            if (nhomVatPham.getLoaiSuCo() != null && nhomVatPham.getLoaiSuCo().getId() != null) {
                idSet.put(nhomVatPham.getLoaiSuCo().getId(), Boolean.TRUE);
            }

            nhomVatPhamLoaiSuCoMap.put(nhomVatPham.getId(), new ArrayList<>(idSet.keySet()));
        }
    }

    @NonNull
    private LoaiSuCoFilterRequestDto buildFilterRequest() {
        List<LoaiSuCoFilterRequestDto.FilterCriteriaDto> filters = new ArrayList<>();
        filters.add(new LoaiSuCoFilterRequestDto.FilterCriteriaDto(
                "trangThai",
                "EQUALS",
                "true",
                "AND"
        ));

        List<LoaiSuCoFilterRequestDto.SortCriteriaDto> sorts = new ArrayList<>();
        sorts.add(new LoaiSuCoFilterRequestDto.SortCriteriaDto("id", "ASC"));

        return new LoaiSuCoFilterRequestDto(filters, sorts, DEFAULT_FILTER_PAGE, DEFAULT_FILTER_SIZE);
    }

    @NonNull
    private List<ReportEmergencyType> mapEmergencyTypes(@NonNull List<LoaiSuCoItemDto> items) {
        List<ReportEmergencyType> mapped = new ArrayList<>();
        for (LoaiSuCoItemDto item : items) {
            if (item == null) {
                continue;
            }
            String label = fallbackIfBlank(item.getTen(), DEFAULT_LABEL);
            String iconUrl = trimToNull(item.getIconUrl());
            mapped.add(new ReportEmergencyType(
                    item.getId(),
                    label,
                    iconUrl,
                    resolveIconResId(label, iconUrl)
            ));
        }
        return mapped;
    }

    @NonNull
    private List<ReportSupplyItem> filterSuppliesByLoaiSuCoId(
            long loaiSuCoId,
            @NonNull List<VatPhamItemDto> vatPhamItems
    ) {
        List<ReportSupplyItem> matched = new ArrayList<>();
        for (VatPhamItemDto item : vatPhamItems) {
            if (item == null || !Boolean.TRUE.equals(item.getTrangThai())) {
                continue;
            }

            VatPhamItemDto.NhomVatPhamDto displayGroup = resolveDisplayGroupForLoaiSuCo(item, loaiSuCoId);
            if (displayGroup == null) {
                continue;
            }

            String name = fallbackIfBlank(item.getTenVatPham(), DEFAULT_SUPPLY_NAME);
            Integer quantity = item.getSoLuong();
            String unitName = null;
            Long groupId;
            String groupName = DEFAULT_GROUP_NAME;
            String imageUrl = null;
            if (item.getDonVi() != null) {
                unitName = trimToNull(item.getDonVi().getTen());
            }
            groupId = displayGroup.getId();
            groupName = fallbackIfBlank(displayGroup.getTen(), DEFAULT_GROUP_NAME);
            if (item.getTepTin() != null) {
                imageUrl = trimToNull(item.getTepTin().getDuongDan());
            }
            matched.add(new ReportSupplyItem(
                    item.getId(),
                    groupId,
                    groupName,
                    name,
                    imageUrl,
                    quantity,
                    unitName
            ));
        }
        matched.sort((left, right) -> {
            int groupCompare = left.getGroupName().compareToIgnoreCase(right.getGroupName());
            if (groupCompare != 0) {
                return groupCompare;
            }
            return left.getName().compareToIgnoreCase(right.getName());
        });
        return matched;
    }

    private VatPhamItemDto.NhomVatPhamDto resolveDisplayGroupForLoaiSuCo(VatPhamItemDto item, long loaiSuCoId) {
        if (item == null) {
            return null;
        }

        List<VatPhamItemDto.NhomVatPhamDto> groups = extractGroups(item);
        if (groups.isEmpty()) {
            return null;
        }

        for (VatPhamItemDto.NhomVatPhamDto group : groups) {
            if (isGroupLinkedToLoaiSuCo(group, loaiSuCoId)) {
                return group;
            }
        }
        return null;
    }

    private boolean isGroupLinkedToLoaiSuCo(VatPhamItemDto.NhomVatPhamDto group, long loaiSuCoId) {
        if (group == null) {
            return false;
        }

        Long nhomVatPhamId = group.getId();
        if (nhomVatPhamId != null) {
            List<Long> loaiSuCoIds = nhomVatPhamLoaiSuCoMap.get(nhomVatPhamId);
            if (loaiSuCoIds != null && !loaiSuCoIds.isEmpty()) {
                for (Long linkedLoaiSuCoId : loaiSuCoIds) {
                    if (Objects.equals(linkedLoaiSuCoId, loaiSuCoId)) {
                        return true;
                    }
                }
                return false;
            }
        }

        List<VatPhamItemDto.LoaiSuCoDto> loaiSuCoList = group.getLoaiSuCos();
        if (loaiSuCoList != null && !loaiSuCoList.isEmpty()) {
            for (VatPhamItemDto.LoaiSuCoDto loaiSuCo : loaiSuCoList) {
                if (loaiSuCo == null) {
                    continue;
                }
                if (Objects.equals(loaiSuCo.getId(), loaiSuCoId)) {
                    return true;
                }
            }
        }

        // Backward compatibility when backend still returns a single loaiSuCo object.
        VatPhamItemDto.LoaiSuCoDto loaiSuCo = group.getLoaiSuCo();
        return loaiSuCo != null && Objects.equals(loaiSuCo.getId(), loaiSuCoId);
    }

    @NonNull
    private List<VatPhamItemDto.NhomVatPhamDto> extractGroups(@NonNull VatPhamItemDto item) {
        List<VatPhamItemDto.NhomVatPhamDto> groups = new ArrayList<>();
        if (item.getNhomVatPhams() != null) {
            for (VatPhamItemDto.NhomVatPhamDto group : item.getNhomVatPhams()) {
                if (group == null) {
                    continue;
                }
                groups.add(group);
            }
        }
        if (item.getNhomVatPham() != null) {
            groups.add(item.getNhomVatPham());
        }

        LinkedHashMap<Long, VatPhamItemDto.NhomVatPhamDto> byId = new LinkedHashMap<>();
        List<VatPhamItemDto.NhomVatPhamDto> withoutId = new ArrayList<>();
        for (VatPhamItemDto.NhomVatPhamDto group : groups) {
            if (group == null) {
                continue;
            }
            Long id = group.getId();
            if (id == null) {
                withoutId.add(group);
                continue;
            }
            if (!byId.containsKey(id)) {
                byId.put(id, group);
            }
        }

        List<VatPhamItemDto.NhomVatPhamDto> result = new ArrayList<>(byId.values());
        result.addAll(withoutId);
        return result;
    }

    private int resolveIconResId(String label, String iconUrl) {
        String source = ((label == null ? "" : label) + " " + (iconUrl == null ? "" : iconUrl))
                .toLowerCase(Locale.ROOT);

        if (containsAny(source, "accident", "tai nan", "va cham", "collision", "crash")) {
            return R.drawable.ic_emergency_accident;
        }
        if (containsAny(source, "fire", "chay", "hoa hoan")) {
            return R.drawable.ic_emergency_fire;
        }
        if (containsAny(source, "medical", "y te", "cap cuu", "benh", "suc khoe")) {
            return R.drawable.ic_emergency_medical;
        }
        if (containsAny(source, "flood", "ngap", "lut", "bao lut")) {
            return R.drawable.ic_emergency_flood;
        }
        if (containsAny(source, "quake", "dong dat", "rung chan")) {
            return R.drawable.ic_emergency_quake;
        }
        if (containsAny(source, "robbery", "cuop", "trom")) {
            return R.drawable.ic_emergency_robbery;
        }
        if (containsAny(source, "assault", "bao luc", "hanh hung")) {
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

    @NonNull
    private String fallbackIfBlank(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    @Nullable
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static final class CreateRescueTicketInput {

        @Nullable
        private final Long loaiSuCoId;

        @Nullable
        private final String reporterName;

        @Nullable
        private final String reporterPhone;

        @Nullable
        private final String address;

        @Nullable
        private final String lat;

        @Nullable
        private final String longitude;

        @Nullable
        private final String ghiChu;

        @NonNull
        private final List<SelectedSupplyInput> selectedSupplies;

        @NonNull
        private final List<AttachmentInput> attachments;

        public CreateRescueTicketInput(
                @Nullable Long loaiSuCoId,
                @Nullable String reporterName,
                @Nullable String reporterPhone,
                @Nullable String address,
                @Nullable String lat,
                @Nullable String longitude,
                @Nullable String ghiChu,
                @Nullable List<SelectedSupplyInput> selectedSupplies,
                @Nullable List<AttachmentInput> attachments
        ) {
            this.loaiSuCoId = loaiSuCoId;
            this.reporterName = reporterName;
            this.reporterPhone = reporterPhone;
            this.address = address;
            this.lat = lat;
            this.longitude = longitude;
            this.ghiChu = ghiChu;
            this.selectedSupplies = selectedSupplies == null ? new ArrayList<>() : selectedSupplies;
            this.attachments = attachments == null ? new ArrayList<>() : attachments;
        }

        @Nullable
        public Long getLoaiSuCoId() {
            return loaiSuCoId;
        }

        @Nullable
        public String getReporterName() {
            return reporterName;
        }

        @Nullable
        public String getReporterPhone() {
            return reporterPhone;
        }

        @Nullable
        public String getAddress() {
            return address;
        }

        @Nullable
        public String getLat() {
            return lat;
        }

        @Nullable
        public String getLongitude() {
            return longitude;
        }

        @Nullable
        public String getGhiChu() {
            return ghiChu;
        }

        @NonNull
        public List<SelectedSupplyInput> getSelectedSupplies() {
            return selectedSupplies;
        }

        @NonNull
        public List<AttachmentInput> getAttachments() {
            return attachments;
        }
    }

    public static final class SelectedSupplyInput {

        @Nullable
        private final Long vatPhamId;

        private final int quantity;

        public SelectedSupplyInput(@Nullable Long vatPhamId, int quantity) {
            this.vatPhamId = vatPhamId;
            this.quantity = Math.max(1, quantity);
        }

        @Nullable
        public Long getVatPhamId() {
            return vatPhamId;
        }

        public int getQuantity() {
            return quantity;
        }
    }

    public static final class AttachmentInput {

        @Nullable
        private final Uri uri;

        @Nullable
        private final String type;

        public AttachmentInput(@Nullable Uri uri, @Nullable String type) {
            this.uri = uri;
            this.type = type;
        }

        @NonNull
        public static AttachmentInput image(@Nullable Uri uri) {
            return new AttachmentInput(uri, ATTACHMENT_TYPE_IMAGE);
        }

        @NonNull
        public static AttachmentInput video(@Nullable Uri uri) {
            return new AttachmentInput(uri, ATTACHMENT_TYPE_VIDEO);
        }

        @NonNull
        public static AttachmentInput audio(@Nullable Uri uri) {
            return new AttachmentInput(uri, ATTACHMENT_TYPE_AUDIO);
        }

        @Nullable
        public Uri getUri() {
            return uri;
        }

        @Nullable
        public String getType() {
            return type;
        }
    }

    private static final class AttachmentUploadTask {

        @NonNull
        private final Uri uri;

        @NonNull
        private final String thuMuc;

        @NonNull
        private final String tenTep;

        @NonNull
        private final String loai;

        private final int thuTu;

        private AttachmentUploadTask(
                @NonNull Uri uri,
                @NonNull String thuMuc,
                @NonNull String tenTep,
                @NonNull String loai,
                int thuTu
        ) {
            this.uri = uri;
            this.thuMuc = thuMuc;
            this.tenTep = tenTep;
            this.loai = loai;
            this.thuTu = Math.max(0, thuTu);
        }

        @NonNull
        public Uri getUri() {
            return uri;
        }

        @NonNull
        public String getThuMuc() {
            return thuMuc;
        }

        @NonNull
        public String getTenTep() {
            return tenTep;
        }

        @NonNull
        public String getLoai() {
            return loai;
        }

        public int getThuTu() {
            return thuTu;
        }
    }

    private static final class UriRequestBody extends RequestBody {

        @NonNull
        private final ContentResolver resolver;

        @NonNull
        private final Uri uri;

        @Nullable
        private final MediaType contentType;

        private UriRequestBody(
                @NonNull ContentResolver resolver,
                @NonNull Uri uri,
                @Nullable MediaType contentType
        ) {
            this.resolver = resolver;
            this.uri = uri;
            this.contentType = contentType;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            AssetFileDescriptor descriptor = null;
            try {
                descriptor = resolver.openAssetFileDescriptor(uri, "r");
                if (descriptor == null) {
                    return -1L;
                }
                return descriptor.getLength();
            } catch (IOException | SecurityException ignored) {
                return -1L;
            } finally {
                if (descriptor != null) {
                    try {
                        descriptor.close();
                    } catch (IOException ignored) {
                        // Ignore.
                    }
                }
            }
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            InputStream inputStream;
            try {
                inputStream = resolver.openInputStream(uri);
            } catch (SecurityException exception) {
                throw new IOException("Cannot open attachment stream", exception);
            }
            if (inputStream == null) {
                throw new IOException("Attachment stream is null");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            try (InputStream stream = inputStream) {
                while ((bytesRead = stream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
