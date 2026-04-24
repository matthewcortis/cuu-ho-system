package com.example.cuutro.core.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AuthSessionManager {

    private static final String PREFS_NAME = "cuutro_auth_session";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_AT_EPOCH_MS = "expires_at_epoch_ms";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final long EXPIRY_SAFE_WINDOW_MS = 60_000L;

    private final SharedPreferences sharedPreferences;

    public AuthSessionManager(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void saveSession(
            @NonNull String tokenType,
            @NonNull String accessToken,
            long expiresAtEpochMs,
            @Nullable Long accountId,
            @Nullable String username,
            @Nullable String role
    ) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN_TYPE, sanitize(tokenType, "Bearer"))
                .putString(KEY_ACCESS_TOKEN, sanitize(accessToken, ""))
                .putLong(KEY_EXPIRES_AT_EPOCH_MS, Math.max(expiresAtEpochMs, 0L))
                .putLong(KEY_ACCOUNT_ID, accountId == null ? -1L : accountId)
                .putString(KEY_USERNAME, sanitizeNullable(username))
                .putString(KEY_ROLE, sanitizeNullable(role))
                .apply();
    }

    public synchronized void saveBearerToken(@NonNull String token) {
        saveSession("Bearer", token, 0L, null, null, null);
    }

    public synchronized boolean hasUsableToken() {
        String accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return false;
        }

        long expiresAtEpochMs = sharedPreferences.getLong(KEY_EXPIRES_AT_EPOCH_MS, 0L);
        if (expiresAtEpochMs <= 0L) {
            return true;
        }

        long now = System.currentTimeMillis();
        return expiresAtEpochMs - EXPIRY_SAFE_WINDOW_MS > now;
    }

    @Nullable
    public synchronized String getAuthorizationHeaderValue() {
        String accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return null;
        }

        String tokenType = sharedPreferences.getString(KEY_TOKEN_TYPE, "Bearer");
        if (tokenType == null || tokenType.trim().isEmpty()) {
            tokenType = "Bearer";
        }
        return tokenType.trim() + " " + accessToken.trim();
    }

    public synchronized void clearSession() {
        sharedPreferences.edit().clear().apply();
    }

    @Nullable
    public synchronized Long getAccountId() {
        long accountId = sharedPreferences.getLong(KEY_ACCOUNT_ID, -1L);
        return accountId > 0L ? accountId : null;
    }

    @Nullable
    public synchronized String getUsername() {
        return sanitizeNullable(sharedPreferences.getString(KEY_USERNAME, null));
    }

    @Nullable
    public synchronized String getRole() {
        return sanitizeNullable(sharedPreferences.getString(KEY_ROLE, null));
    }

    private String sanitize(@Nullable String value, @NonNull String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    @Nullable
    private String sanitizeNullable(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
