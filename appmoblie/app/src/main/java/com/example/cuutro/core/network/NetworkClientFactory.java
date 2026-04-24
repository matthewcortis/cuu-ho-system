package com.example.cuutro.core.network;

import androidx.annotation.NonNull;

import com.example.cuutro.BuildConfig;
import com.example.cuutro.core.auth.AuthSessionManager;
import com.example.cuutro.core.network.interceptor.AuthTokenInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkClientFactory {

    private static final long CONNECT_TIMEOUT_SECONDS = 20L;
    private static final long READ_TIMEOUT_SECONDS = 30L;
    private static final long WRITE_TIMEOUT_SECONDS = 30L;

    private NetworkClientFactory() {
    }

    @NonNull
    public static Retrofit createRetrofit(@NonNull AuthSessionManager authSessionManager) {
        Gson gson = new GsonBuilder().create();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(new MetadataInterceptor())
                .addInterceptor(new AuthTokenInterceptor(authSessionManager));

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        clientBuilder.addInterceptor(loggingInterceptor);

        return new Retrofit.Builder()
                .baseUrl(BackendConfig.getBaseUrl())
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private static class MetadataInterceptor implements Interceptor {

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            Request enriched = request.newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "cuutro-android/" + BuildConfig.VERSION_NAME)
                    .build();
            return chain.proceed(enriched);
        }
    }
}
