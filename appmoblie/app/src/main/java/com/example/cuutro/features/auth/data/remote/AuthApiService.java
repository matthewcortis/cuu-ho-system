package com.example.cuutro.features.auth.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.auth.data.remote.dto.ForgotPasswordRequestDto;
import com.example.cuutro.features.auth.data.remote.dto.LoginRequestDto;
import com.example.cuutro.features.auth.data.remote.dto.LoginResponseDto;
import com.example.cuutro.features.auth.data.remote.dto.RegisterRequestDto;
import com.example.cuutro.features.auth.data.remote.dto.RegisterResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("auth/login")
    Call<ApiEnvelope<LoginResponseDto>> login(
            @Header("No-Auth") String skipAuth,
            @Body LoginRequestDto request
    );

    @POST("auth/register")
    Call<ApiEnvelope<RegisterResponseDto>> register(
            @Header("No-Auth") String skipAuth,
            @Body RegisterRequestDto request
    );

    @POST("auth/forgot-password")
    Call<ApiEnvelope<Object>> forgotPassword(
            @Header("No-Auth") String skipAuth,
            @Body ForgotPasswordRequestDto request
    );
}
