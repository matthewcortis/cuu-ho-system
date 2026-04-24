package com.example.cuutro.features.report.ui.controller;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportBitmapLoader {

    public interface Callback {
        void onLoaded(@Nullable String normalizedUrl, @Nullable Bitmap bitmap);
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Map<String, Bitmap> bitmapCache = new ConcurrentHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void load(@Nullable String rawUrl, @NonNull Callback callback) {
        String normalizedUrl = normalizeUrl(rawUrl);
        if (!isHttpUrl(normalizedUrl)) {
            callback.onLoaded(normalizedUrl, null);
            return;
        }

        Bitmap cachedBitmap = bitmapCache.get(normalizedUrl);
        if (cachedBitmap != null) {
            callback.onLoaded(normalizedUrl, cachedBitmap);
            return;
        }

        executorService.execute(() -> {
            Bitmap bitmap = downloadBitmap(normalizedUrl);
            if (bitmap != null) {
                bitmapCache.put(normalizedUrl, bitmap);
            }
            mainHandler.post(() -> callback.onLoaded(normalizedUrl, bitmap));
        });
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    @Nullable
    public String normalizeUrl(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public boolean isHttpUrl(@Nullable String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
    }

    @Nullable
    private Bitmap downloadBitmap(@NonNull String imageUrl) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(8000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                return null;
            }
            inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
