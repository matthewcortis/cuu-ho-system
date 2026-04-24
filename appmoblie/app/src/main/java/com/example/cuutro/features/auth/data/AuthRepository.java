package com.example.cuutro.features.auth.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Build;

import com.example.cuutro.core.auth.AuthSessionManager;
import com.example.cuutro.core.network.BackendConfig;
import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.remote.AuthApiService;
import com.example.cuutro.features.auth.data.remote.dto.LoginRequestDto;
import com.example.cuutro.features.auth.data.remote.dto.LoginResponseDto;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;

public class AuthRepository {

    private static final String DEFAULT_TOKEN_TYPE = "Bearer";
    private static final String MISSING_AUTH_CONFIG_MESSAGE =
            "Chưa cấu hình backendBearerToken hoặc backendUsername/backendPassword.";
    private static final String ROLE_NGUOI_DAN = "NGUOI_DAN";
    private static final String ROLE_TRUONG_NHOM_TNV = "TRUONG_NHOM_TNV";
    private static final Set<String> SUPPORTED_APP_ROLES =
            Set.of(ROLE_NGUOI_DAN, ROLE_TRUONG_NHOM_TNV);

    private final AuthApiService authApiService;
    private final AuthSessionManager authSessionManager;
    private final NetworkCallExecutor networkCallExecutor;

    public AuthRepository(
            @NonNull AuthApiService authApiService,
            @NonNull AuthSessionManager authSessionManager,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.authApiService = authApiService;
        this.authSessionManager = authSessionManager;
        this.networkCallExecutor = networkCallExecutor;
    }

    public boolean hasActiveSession() {
        if (!authSessionManager.hasUsableToken()) {
            return false;
        }
        String role = normalizeRole(authSessionManager.getRole());
        if (!role.isEmpty() && !isSupportedAppRole(role)) {
            authSessionManager.clearSession();
            return false;
        }
        return true;
    }

    @Nullable
    public Long getCurrentTaiKhoanId() {
        return authSessionManager.getAccountId();
    }

    @Nullable
    public String getCurrentUsername() {
        return authSessionManager.getUsername();
    }

    @Nullable
    public String getCurrentRole() {
        String role = normalizeRole(authSessionManager.getRole());
        return role.isEmpty() ? null : role;
    }

    public void login(
            @Nullable String usernameOrEmail,
            @Nullable String password,
            @NonNull ResultCallback<Void> callback
    ) {
        String safeUsername = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        String safePassword = password == null ? "" : password.trim();
        if (safeUsername.isEmpty() || safePassword.isEmpty()) {
            callback.onError(new NetworkError(
                    400,
                    "Vui lòng nhập tài khoản và mật khẩu"
            ));
            return;
        }
        requestLogin(new LoginRequestDto(safeUsername, safePassword), callback);
    }

    public void ensureAuthenticated(@NonNull ResultCallback<Void> callback) {
        if (authSessionManager.hasUsableToken()) {
            callback.onSuccess(null);
            return;
        }

        String staticToken = BackendConfig.getStaticBearerToken();
        if (!staticToken.isEmpty()) {
            authSessionManager.saveBearerToken(staticToken);
            callback.onSuccess(null);
            return;
        }

        if (!BackendConfig.hasBootstrapCredentials()) {
            callback.onError(new NetworkError(401, MISSING_AUTH_CONFIG_MESSAGE));
            return;
        }

        LoginRequestDto request = new LoginRequestDto(
                BackendConfig.getBootstrapUsername(),
                BackendConfig.getBootstrapPassword()
        );
        requestLogin(request, callback);
    }

    private void requestLogin(
            @NonNull LoginRequestDto request,
            @NonNull ResultCallback<Void> callback
    ) {
        networkCallExecutor.execute(
                authApiService.login("true", request),
                new ResultCallback<LoginResponseDto>() {
                    @Override
                    public void onSuccess(LoginResponseDto data) {
                        if (data == null || isBlank(data.getAccessToken())) {
                            callback.onError(new NetworkError(
                                    NetworkError.CODE_UNKNOWN,
                                    "Đăng nhập backend thất bại: thiếu access token"
                            ));
                            return;
                        }
                        String normalizedRole = normalizeRole(data.getVaiTro());
                        if (!isSupportedAppRole(normalizedRole)) {
                            authSessionManager.clearSession();
                            callback.onError(new NetworkError(
                                    403,
                                    "Ứng dụng chỉ hỗ trợ role NGUOI_DAN hoặc TRUONG_NHOM_TNV"
                            ));
                            return;
                        }
                        saveSession(data);
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        callback.onError(error);
                    }
                }
        );
    }

    private void saveSession(@NonNull LoginResponseDto data) {
        authSessionManager.saveSession(
                fallbackIfBlank(data.getTokenType(), DEFAULT_TOKEN_TYPE),
                data.getAccessToken().trim(),
                parseExpiresAt(data.getExpiresAt()),
                data.getTaiKhoanId(),
                data.getTenDangNhap(),
                normalizeRole(data.getVaiTro())
        );
    }

    public void clearSession() {
        authSessionManager.clearSession();
    }

    private long parseExpiresAt(String rawExpiresAt) {
        if (isBlank(rawExpiresAt)) {
            return 0L;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return 0L;
        }
        try {
            return Instant.parse(rawExpiresAt.trim()).toEpochMilli();
        } catch (DateTimeParseException ignored) {
            return 0L;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String fallbackIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private String normalizeRole(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isSupportedAppRole(String role) {
        return SUPPORTED_APP_ROLES.contains(role);
    }
}
