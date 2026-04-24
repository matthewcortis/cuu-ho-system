package com.example.cuutro.core.network;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class NetworkCallExecutor {

    private static final Type GENERIC_ENVELOPE_TYPE = new TypeToken<ApiEnvelope<Object>>() {
    }.getType();

    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(3);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    public <T> void execute(@NonNull Call<ApiEnvelope<T>> call, @NonNull ResultCallback<T> callback) {
        ioExecutor.execute(() -> {
            CallbackResult<T> result = runCall(call);
            mainHandler.post(() -> {
                if (result.error == null) {
                    callback.onSuccess(result.data);
                    return;
                }
                callback.onError(result.error);
            });
        });
    }

    @NonNull
    private <T> CallbackResult<T> runCall(@NonNull Call<ApiEnvelope<T>> call) {
        try {
            Response<ApiEnvelope<T>> response = call.execute();
            if (!response.isSuccessful()) {
                String message = parseErrorMessage(response.errorBody(), response.code());
                return CallbackResult.failure(new NetworkError(response.code(), message));
            }

            ApiEnvelope<T> body = response.body();
            if (body == null) {
                return CallbackResult.failure(new NetworkError(
                        response.code(),
                        "Phản hồi từ máy chủ không hợp lệ"
                ));
            }

            if (body.getStatus() >= 400) {
                String message = firstNonBlank(body.getMessage(), body.getError(), "Yêu cầu thất bại");
                return CallbackResult.failure(new NetworkError(body.getStatus(), message));
            }

            return CallbackResult.success(body.getData());
        } catch (IOException exception) {
            return CallbackResult.failure(new NetworkError(
                    NetworkError.CODE_UNKNOWN,
                    "Không thể kết nối máy chủ. Vui lòng kiểm tra mạng."
            ));
        } catch (Exception exception) {
            return CallbackResult.failure(new NetworkError(
                    NetworkError.CODE_UNKNOWN,
                    "Đã xảy ra lỗi khi xử lý dữ liệu từ máy chủ"
            ));
        }
    }

    @NonNull
    private String parseErrorMessage(ResponseBody errorBody, int statusCode) {
        if (errorBody == null) {
            return "Yêu cầu thất bại (HTTP " + statusCode + ")";
        }

        try {
            String raw = errorBody.string();
            if (raw == null || raw.trim().isEmpty()) {
                return "Yêu cầu thất bại (HTTP " + statusCode + ")";
            }

            ApiEnvelope<?> envelope = gson.fromJson(raw, GENERIC_ENVELOPE_TYPE);
            if (envelope != null) {
                String message = firstNonBlank(envelope.getMessage(), envelope.getError(), null);
                if (message != null) {
                    return message;
                }
            }
            return raw;
        } catch (Exception ignored) {
            return "Yêu cầu thất bại (HTTP " + statusCode + ")";
        }
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }
        return fallback;
    }

    private static final class CallbackResult<T> {

        private final T data;
        private final NetworkError error;

        private CallbackResult(T data, NetworkError error) {
            this.data = data;
            this.error = error;
        }

        @NonNull
        static <T> CallbackResult<T> success(T data) {
            return new CallbackResult<>(data, null);
        }

        @NonNull
        static <T> CallbackResult<T> failure(@NonNull NetworkError error) {
            return new CallbackResult<>(null, error);
        }
    }
}
