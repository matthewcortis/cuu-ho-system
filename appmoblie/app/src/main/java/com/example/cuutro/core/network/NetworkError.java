package com.example.cuutro.core.network;

import androidx.annotation.NonNull;

public final class NetworkError {

    public static final int CODE_UNKNOWN = -1;

    private final int statusCode;
    private final String message;

    public NetworkError(int statusCode, @NonNull String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public boolean isUnauthorized() {
        return statusCode == 401;
    }
}
