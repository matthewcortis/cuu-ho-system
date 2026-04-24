package com.example.cuutro.core.network.interceptor;

import androidx.annotation.NonNull;

import com.example.cuutro.core.auth.AuthSessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthTokenInterceptor implements Interceptor {

    private static final String HEADER_SKIP_AUTH = "No-Auth";

    private final AuthSessionManager authSessionManager;

    public AuthTokenInterceptor(@NonNull AuthSessionManager authSessionManager) {
        this.authSessionManager = authSessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        boolean skipAuth = "true".equalsIgnoreCase(request.header(HEADER_SKIP_AUTH));

        Request.Builder builder = request.newBuilder().removeHeader(HEADER_SKIP_AUTH);
        if (!skipAuth) {
            String authorizationHeader = authSessionManager.getAuthorizationHeaderValue();
            if (authorizationHeader != null && !authorizationHeader.trim().isEmpty()) {
                builder.header("Authorization", authorizationHeader);
            }
        }

        return chain.proceed(builder.build());
    }
}
