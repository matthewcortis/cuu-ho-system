package com.example.cuutro.core.network;

import androidx.annotation.NonNull;

import com.example.cuutro.BuildConfig;

public final class BackendConfig {

    private static final String DEFAULT_BASE_URL = "http://192.168.100.90:8080/";

    private BackendConfig() {
    }

    @NonNull
    public static String getBaseUrl() {
        String raw = sanitize(BuildConfig.BACKEND_BASE_URL);
        if (raw.isEmpty()) {
            return DEFAULT_BASE_URL;
        }
        return raw.endsWith("/") ? raw : raw + "/";
    }

    @NonNull
    public static String getStaticBearerToken() {
        return sanitize(BuildConfig.BACKEND_STATIC_BEARER_TOKEN);
    }

    @NonNull
    public static String getBootstrapUsername() {
        return sanitize(BuildConfig.BACKEND_USERNAME);
    }

    @NonNull
    public static String getBootstrapPassword() {
        return sanitize(BuildConfig.BACKEND_PASSWORD);
    }

    public static boolean hasBootstrapCredentials() {
        return !getBootstrapUsername().isEmpty() && !getBootstrapPassword().isEmpty();
    }

    @NonNull
    private static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
