package com.example.cuutro.features.profile.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.profile.data.remote.dto.NguoiDungDoiMatKhauRequestDto;
import com.example.cuutro.features.profile.data.remote.dto.NguoiDungResponseDto;
import com.example.cuutro.features.profile.data.remote.dto.NguoiDungUpsertRequestDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PUT;

public interface ProfileApiService {

    @GET("nguoi-dung/me")
    Call<ApiEnvelope<NguoiDungResponseDto>> getCurrentNguoiDung();

	@PUT("nguoi-dung/me")
	Call<ApiEnvelope<NguoiDungResponseDto>> updateCurrentNguoiDung(@Body NguoiDungUpsertRequestDto request);

	@PUT("nguoi-dung/me/doi-mat-khau")
	Call<ApiEnvelope<Void>> changeCurrentNguoiDungPassword(@Body NguoiDungDoiMatKhauRequestDto request);

    @DELETE("nguoi-dung/me")
    Call<ApiEnvelope<Void>> deleteCurrentNguoiDung();

    @Multipart
    @POST("nguoi-dung/me/avatar")
    Call<ApiEnvelope<NguoiDungResponseDto>> uploadCurrentNguoiDungAvatar(
            @Part MultipartBody.Part avatar
    );
}
