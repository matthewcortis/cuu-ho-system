package com.example.cuutro.features.profile.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.profile.data.model.UserProfileData;
import com.example.cuutro.features.profile.data.remote.ProfileApiService;
import com.example.cuutro.features.profile.data.remote.dto.NguoiDungDoiMatKhauRequestDto;
import com.example.cuutro.features.profile.data.remote.dto.NguoiDungResponseDto;
import com.example.cuutro.features.profile.data.remote.dto.NguoiDungUpsertRequestDto;
import com.example.cuutro.features.profile.data.remote.dto.TinhNguyenVienDangKyRequestDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProfileRepository {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private static final String PREFS_NAME = "profile_local_storage";
    private static final String KEY_PROFILE_EXISTS = "profile_exists";
    private static final String KEY_NGUOI_DUNG_ID = "nguoi_dung_id";
    private static final String KEY_TAI_KHOAN_ID = "tai_khoan_id";
    private static final String KEY_TEN_DANG_NHAP = "ten_dang_nhap";
    private static final String KEY_HO_TEN = "ho_ten";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_SO_DIEN_THOAI = "so_dien_thoai";
    private static final String KEY_DIA_CHI = "dia_chi";
    private static final String KEY_VI_DO = "vi_do";
    private static final String KEY_KINH_DO = "kinh_do";
    private static final String KEY_AVATAR_URI = "avatar_uri";

    private final ProfileApiService profileApiService;
    private final AuthRepository authRepository;
    private final NetworkCallExecutor networkCallExecutor;
    private final SharedPreferences profilePreferences;
    private final Context appContext;

    public ProfileRepository(
            @NonNull Context context,
            @NonNull ProfileApiService profileApiService,
            @NonNull AuthRepository authRepository,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.profileApiService = profileApiService;
        this.authRepository = authRepository;
        this.networkCallExecutor = networkCallExecutor;
        this.appContext = context.getApplicationContext();
        this.profilePreferences = appContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void getCurrentUserProfile(@NonNull ResultCallback<UserProfileData> callback) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Phiên đăng nhập đã hết hạn"));
            return;
        }

        UserProfileData localProfile = readLocalProfile();
        networkCallExecutor.execute(
                profileApiService.getCurrentNguoiDung(),
                new ResultCallback<NguoiDungResponseDto>() {
                    @Override
                    public void onSuccess(NguoiDungResponseDto data) {
                        if (data == null) {
                            if (localProfile != null) {
                                callback.onSuccess(localProfile);
                                return;
                            }
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    "Không tìm thấy dữ liệu người dùng"
                            ));
                            return;
                        }
                        UserProfileData remoteProfile = toUserProfileData(data);
                        UserProfileData mergedProfile = mergeRemoteAndLocal(remoteProfile, localProfile);
                        persistLocalProfile(mergedProfile);
                        callback.onSuccess(mergedProfile);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (!error.isUnauthorized() && error.getStatusCode() != 403 && localProfile != null) {
                            callback.onSuccess(localProfile);
                            return;
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    public void createOrUpdateCurrentUserProfile(
            @NonNull UserProfileData draftProfile,
            @NonNull ResultCallback<UserProfileData> callback
    ) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Phiên đăng nhập đã hết hạn"));
            return;
        }
        UserProfileData normalizedProfile = normalizeProfileForLocalStorage(draftProfile);
        Long taiKhoanId = normalizedProfile.getTaiKhoanId();
        if (taiKhoanId == null || taiKhoanId <= 0) {
            callback.onError(new NetworkError(
                    400,
                    "Không tìm thấy tài khoản đăng nhập để lưu hồ sơ"
            ));
            return;
        }

        String avatarUrl = trimToNull(normalizedProfile.getAvatarUrl());
        if (isLocalOnlyAvatarUri(avatarUrl)) {
            uploadAvatarThenUpdateProfile(normalizedProfile, avatarUrl, callback);
            return;
        }
        updateCurrentProfileToServer(normalizedProfile, callback);
    }

    public void changeCurrentUserPassword(
            @Nullable String currentPassword,
            @Nullable String newPassword,
            @NonNull ResultCallback<Void> callback
    ) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Phiên đăng nhập đã hết hạn"));
            return;
        }

        String safeCurrentPassword = currentPassword == null ? "" : currentPassword;
        String safeNewPassword = newPassword == null ? "" : newPassword;
        if (safeCurrentPassword.trim().isEmpty()) {
            callback.onError(new NetworkError(400, "Vui lòng nhập mật khẩu hiện tại"));
            return;
        }
        if (safeNewPassword.trim().isEmpty()) {
            callback.onError(new NetworkError(400, "Vui lòng nhập mật khẩu mới"));
            return;
        }
        if (safeNewPassword.length() < MIN_PASSWORD_LENGTH) {
            callback.onError(new NetworkError(400, "Mật khẩu mới phải có ít nhất 6 ký tự"));
            return;
        }
        if (safeNewPassword.equals(safeCurrentPassword)) {
            callback.onError(new NetworkError(400, "Mật khẩu mới phải khác mật khẩu hiện tại"));
            return;
        }

        NguoiDungDoiMatKhauRequestDto requestDto = new NguoiDungDoiMatKhauRequestDto(
                safeCurrentPassword,
                safeNewPassword
        );
        networkCallExecutor.execute(
                profileApiService.changeCurrentNguoiDungPassword(requestDto),
                new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        callback.onError(error);
                    }
                }
        );
    }

    public void deleteCurrentUserProfile(@NonNull ResultCallback<Void> callback) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Phiên đăng nhập đã hết hạn"));
            return;
        }
        networkCallExecutor.execute(profileApiService.deleteCurrentNguoiDung(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                clearCurrentLocalProfile();
                callback.onSuccess(null);
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (error.getStatusCode() == 404) {
                    clearCurrentLocalProfile();
                    callback.onSuccess(null);
                    return;
                }
                callback.onError(error);
            }
        });
    }

    public void registerCurrentUserAsVolunteer(
            @NonNull UserProfileData profile,
            @NonNull ResultCallback<Void> callback
    ) {
        registerCurrentUserAsVolunteer(profile, null, null, callback);
    }

    public void registerCurrentUserAsVolunteer(
            @NonNull UserProfileData profile,
            @Nullable String coTheGiup,
            @Nullable String ghiChu,
            @NonNull ResultCallback<Void> callback
    ) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Phiên đăng nhập đã hết hạn"));
            return;
        }
        if (authRepository.isCurrentRoleCaptain()) {
            callback.onError(new NetworkError(400, "Tài khoản hiện đã là tình nguyện viên"));
            return;
        }

        String nguoiDungId = trimToNull(profile.getNguoiDungId());
        if (nguoiDungId == null) {
            callback.onError(new NetworkError(
                    400,
                    "Không tìm thấy hồ sơ người dùng để đăng ký tình nguyện viên"
            ));
            return;
        }

        TinhNguyenVienDangKyRequestDto requestDto = new TinhNguyenVienDangKyRequestDto(
                nguoiDungId,
                null,
                trimToNull(ghiChu),
                trimToNull(coTheGiup)
        );
        networkCallExecutor.execute(
                profileApiService.dangKyTinhNguyenVien(requestDto),
                new ResultCallback<Object>() {
                    @Override
                    public void onSuccess(Object data) {
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        callback.onError(error);
                    }
                }
        );
    }

    private void clearCurrentLocalProfile() {
        String prefix = resolveProfilePrefix();
        if (prefix != null) {
            clearLocalProfile(prefix);
        }
    }

    private void uploadAvatarThenUpdateProfile(
            @NonNull UserProfileData normalizedProfile,
            @NonNull String localAvatarUri,
            @NonNull ResultCallback<UserProfileData> callback
    ) {
        MultipartBody.Part avatarPart;
        try {
            avatarPart = createAvatarMultipartPart(localAvatarUri);
        } catch (Exception exception) {
            callback.onError(new NetworkError(
                    NetworkError.CODE_UNKNOWN,
                    "Không đọc được ảnh đại diện để tải lên"
            ));
            return;
        }

        networkCallExecutor.execute(
                profileApiService.uploadCurrentNguoiDungAvatar(avatarPart),
                new ResultCallback<NguoiDungResponseDto>() {
                    @Override
                    public void onSuccess(NguoiDungResponseDto data) {
                        if (data == null) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    "Không nhận được dữ liệu avatar sau khi tải lên"
                            ));
                            return;
                        }
                        String uploadedAvatarUrl = trimToNull(data.getAvatarUrl());
                        if (uploadedAvatarUrl == null) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    "Tải ảnh đại diện thất bại"
                            ));
                            return;
                        }
                        updateCurrentProfileToServer(
                                withAvatarUrl(normalizedProfile, uploadedAvatarUrl),
                                callback
                        );
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        callback.onError(error);
                    }
                }
        );
    }

    private void updateCurrentProfileToServer(
            @NonNull UserProfileData normalizedProfile,
            @NonNull ResultCallback<UserProfileData> callback
    ) {
        NguoiDungUpsertRequestDto requestDto = toNguoiDungUpsertRequest(normalizedProfile);
        networkCallExecutor.execute(
                profileApiService.updateCurrentNguoiDung(requestDto),
                new ResultCallback<NguoiDungResponseDto>() {
                    @Override
                    public void onSuccess(NguoiDungResponseDto data) {
                        if (data == null) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    "Không nhận được dữ liệu người dùng sau khi cập nhật"
                            ));
                            return;
                        }
                        UserProfileData mergedProfile = mergeRemoteAndLocal(
                                toUserProfileData(data),
                                normalizedProfile
                        );
                        persistLocalProfile(mergedProfile);
                        callback.onSuccess(mergedProfile);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        callback.onError(error);
                    }
                }
        );
    }

    @NonNull
    private UserProfileData withAvatarUrl(
            @NonNull UserProfileData source,
            @Nullable String avatarUrl
    ) {
        return new UserProfileData(
                source.getNguoiDungId(),
                source.getTaiKhoanId(),
                source.getTenDangNhap(),
                source.getHoTen(),
                source.getEmail(),
                source.getSoDienThoai(),
                source.getDiaChi(),
                source.getViDo(),
                source.getKinhDo(),
                trimToNull(avatarUrl)
        );
    }

    @NonNull
    private MultipartBody.Part createAvatarMultipartPart(@NonNull String localAvatarUri) throws IOException {
        Uri avatarUri = Uri.parse(localAvatarUri);
        ContentResolver contentResolver = appContext.getContentResolver();
        byte[] avatarBytes;
        try (InputStream inputStream = contentResolver.openInputStream(avatarUri)) {
            if (inputStream == null) {
                throw new IOException("Avatar input stream is null");
            }
            avatarBytes = readAllBytes(inputStream);
        }
        if (avatarBytes.length == 0) {
            throw new IOException("Avatar file is empty");
        }

        String mimeType = trimToNull(contentResolver.getType(avatarUri));
        String extension = resolveImageExtension(mimeType);
        String fileName = "avatar-" + System.currentTimeMillis() + "." + extension;

        RequestBody avatarRequestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return avatarBytes.length;
            }

            @Override
            public void writeTo(@NonNull BufferedSink sink) throws IOException {
                sink.write(avatarBytes);
            }
        };
        return MultipartBody.Part.createFormData("avatar", fileName, avatarRequestBody);
    }

    @NonNull
    private byte[] readAllBytes(@NonNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    @NonNull
    private String resolveImageExtension(@Nullable String mimeType) {
        if (mimeType == null) {
            return "jpg";
        }
        String normalized = mimeType.toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            case "image/gif":
                return "gif";
            case "image/bmp":
                return "bmp";
            case "image/jpeg":
            case "image/jpg":
            default:
                return "jpg";
        }
    }

    @Nullable
    private UserProfileData readLocalProfile() {
        String prefix = resolveProfilePrefix();
        if (prefix == null || !profilePreferences.getBoolean(prefix + KEY_PROFILE_EXISTS, false)) {
            return null;
        }

        long rawTaiKhoanId = profilePreferences.getLong(prefix + KEY_TAI_KHOAN_ID, -1L);
        Long taiKhoanId = rawTaiKhoanId > 0 ? rawTaiKhoanId : authRepository.getCurrentTaiKhoanId();
        String tenDangNhap = firstNonBlank(
                profilePreferences.getString(prefix + KEY_TEN_DANG_NHAP, null),
                authRepository.getCurrentUsername()
        );
        return new UserProfileData(
                trimToNull(profilePreferences.getString(prefix + KEY_NGUOI_DUNG_ID, null)),
                taiKhoanId,
                tenDangNhap,
                trimToNull(profilePreferences.getString(prefix + KEY_HO_TEN, null)),
                trimToNull(profilePreferences.getString(prefix + KEY_EMAIL, null)),
                normalizePhoneDigits(profilePreferences.getString(prefix + KEY_SO_DIEN_THOAI, null)),
                trimToNull(profilePreferences.getString(prefix + KEY_DIA_CHI, null)),
                trimToNull(profilePreferences.getString(prefix + KEY_VI_DO, null)),
                trimToNull(profilePreferences.getString(prefix + KEY_KINH_DO, null)),
                trimToNull(profilePreferences.getString(prefix + KEY_AVATAR_URI, null))
        );
    }

    private void persistLocalProfile(@NonNull UserProfileData profile) {
        String prefix = resolveProfilePrefix(profile.getTaiKhoanId(), profile.getTenDangNhap());
        if (prefix == null) {
            prefix = resolveProfilePrefix();
        }
        if (prefix == null) {
            return;
        }

        profilePreferences.edit()
                .putBoolean(prefix + KEY_PROFILE_EXISTS, true)
                .putString(prefix + KEY_NGUOI_DUNG_ID, trimToNull(profile.getNguoiDungId()))
                .putLong(prefix + KEY_TAI_KHOAN_ID, profile.getTaiKhoanId() != null ? profile.getTaiKhoanId() : -1L)
                .putString(prefix + KEY_TEN_DANG_NHAP, trimToNull(profile.getTenDangNhap()))
                .putString(prefix + KEY_HO_TEN, trimToNull(profile.getHoTen()))
                .putString(prefix + KEY_EMAIL, trimToNull(profile.getEmail()))
                .putString(prefix + KEY_SO_DIEN_THOAI, normalizePhoneDigits(profile.getSoDienThoai()))
                .putString(prefix + KEY_DIA_CHI, trimToNull(profile.getDiaChi()))
                .putString(prefix + KEY_VI_DO, trimToNull(profile.getViDo()))
                .putString(prefix + KEY_KINH_DO, trimToNull(profile.getKinhDo()))
                .putString(prefix + KEY_AVATAR_URI, trimToNull(profile.getAvatarUrl()))
                .apply();
    }

    private void clearLocalProfile(@NonNull String prefix) {
        profilePreferences.edit()
                .remove(prefix + KEY_PROFILE_EXISTS)
                .remove(prefix + KEY_NGUOI_DUNG_ID)
                .remove(prefix + KEY_TAI_KHOAN_ID)
                .remove(prefix + KEY_TEN_DANG_NHAP)
                .remove(prefix + KEY_HO_TEN)
                .remove(prefix + KEY_EMAIL)
                .remove(prefix + KEY_SO_DIEN_THOAI)
                .remove(prefix + KEY_DIA_CHI)
                .remove(prefix + KEY_VI_DO)
                .remove(prefix + KEY_KINH_DO)
                .remove(prefix + KEY_AVATAR_URI)
                .apply();
    }

    @NonNull
    private UserProfileData normalizeProfileForLocalStorage(@NonNull UserProfileData draft) {
        Long taiKhoanId = firstNonNull(draft.getTaiKhoanId(), authRepository.getCurrentTaiKhoanId());
        String tenDangNhap = firstNonBlank(draft.getTenDangNhap(), authRepository.getCurrentUsername());
        return new UserProfileData(
                trimToNull(draft.getNguoiDungId()),
                taiKhoanId,
                tenDangNhap,
                trimToNull(draft.getHoTen()),
                trimToNull(draft.getEmail()),
                normalizePhoneDigits(draft.getSoDienThoai()),
                trimToNull(draft.getDiaChi()),
                trimToNull(draft.getViDo()),
                trimToNull(draft.getKinhDo()),
                trimToNull(draft.getAvatarUrl())
        );
    }

    @NonNull
    private UserProfileData mergeRemoteAndLocal(
            @NonNull UserProfileData remoteProfile,
            @Nullable UserProfileData localProfile
    ) {
        if (localProfile == null) {
            return remoteProfile;
        }
        return new UserProfileData(
                firstNonBlank(remoteProfile.getNguoiDungId(), localProfile.getNguoiDungId()),
                firstNonNull(remoteProfile.getTaiKhoanId(), localProfile.getTaiKhoanId()),
                firstNonBlank(remoteProfile.getTenDangNhap(), localProfile.getTenDangNhap()),
                localProfile.getHoTen(),
                localProfile.getEmail(),
                localProfile.getSoDienThoai(),
                localProfile.getDiaChi(),
                localProfile.getViDo(),
                localProfile.getKinhDo(),
                localProfile.getAvatarUrl()
        );
    }

    @Nullable
    private String resolveProfilePrefix() {
        return resolveProfilePrefix(
                authRepository.getCurrentTaiKhoanId(),
                authRepository.getCurrentUsername()
        );
    }

    @Nullable
    private String resolveProfilePrefix(@Nullable Long taiKhoanId, @Nullable String tenDangNhap) {
        if (taiKhoanId != null && taiKhoanId > 0) {
            return "tk_" + taiKhoanId + "_";
        }
        String normalizedUsername = trimToNull(tenDangNhap);
        if (normalizedUsername == null) {
            return null;
        }
        return "u_" + normalizedUsername.toLowerCase(Locale.ROOT) + "_";
    }

    @NonNull
    private UserProfileData toUserProfileData(@NonNull NguoiDungResponseDto user) {
        NguoiDungResponseDto.TaiKhoanDto taiKhoan = user.getTaiKhoan();
        NguoiDungResponseDto.ViTriDto viTri = user.getViTri();
        return new UserProfileData(
                trimToNull(user.getId()),
                taiKhoan != null ? taiKhoan.getId() : null,
                taiKhoan != null ? trimToNull(taiKhoan.getTenDangNhap()) : null,
                trimToNull(user.getTen()),
                taiKhoan != null ? trimToNull(taiKhoan.getEmail()) : null,
                trimToNull(user.getSdt()),
                viTri != null ? trimToNull(viTri.getDiaChi()) : null,
                viTri != null ? trimToNull(viTri.getLat()) : null,
                viTri != null ? trimToNull(viTri.getLongitude()) : null,
                trimToNull(user.getAvatarUrl())
        );
    }

    @NonNull
    private NguoiDungUpsertRequestDto toNguoiDungUpsertRequest(@NonNull UserProfileData profile) {
        NguoiDungUpsertRequestDto.ViTriInputDto viTri = null;
        String diaChi = trimToNull(profile.getDiaChi());
        if (diaChi != null) {
            viTri = new NguoiDungUpsertRequestDto.ViTriInputDto(
                    diaChi,
                    trimToNull(profile.getViDo()),
                    trimToNull(profile.getKinhDo())
            );
        }
        String avatarUrl = trimToNull(profile.getAvatarUrl());
        if (isLocalOnlyAvatarUri(avatarUrl)) {
            avatarUrl = null;
        }
        return new NguoiDungUpsertRequestDto(
                profile.getTaiKhoanId(),
                trimToNull(profile.getHoTen()),
                trimToNull(profile.getEmail()),
                normalizePhoneDigits(profile.getSoDienThoai()),
                viTri,
                avatarUrl
        );
    }

    private boolean isLocalOnlyAvatarUri(@Nullable String avatarUrl) {
        if (avatarUrl == null) {
            return false;
        }
        try {
            Uri uri = Uri.parse(avatarUrl);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            return "content".equalsIgnoreCase(scheme) || "file".equalsIgnoreCase(scheme);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Nullable
    private Long firstNonNull(@Nullable Long first, @Nullable Long second) {
        return first != null ? first : second;
    }

    @Nullable
    private String firstNonBlank(@Nullable String first, @Nullable String second) {
        String normalizedFirst = trimToNull(first);
        if (normalizedFirst != null) {
            return normalizedFirst;
        }
        return trimToNull(second);
    }

    @Nullable
    private String normalizePhoneDigits(@Nullable String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        String digits = rawPhone.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() > 10) {
            return digits.substring(0, 10);
        }
        return digits;
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
