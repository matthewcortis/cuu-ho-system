package com.example.cuutro.features.community.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.community.data.remote.dto.BangTinCreateRequestDto;
import com.example.cuutro.features.community.data.remote.dto.BangTinItemDto;
import com.example.cuutro.features.report.data.remote.dto.TepTinUploadResponseDto;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CommunityApiService {

    @GET("bang-tin")
    Call<ApiEnvelope<List<BangTinItemDto>>> getPublicPosts();

    @Multipart
    @POST("tep-tin/upload")
    Call<ApiEnvelope<TepTinUploadResponseDto>> uploadAttachment(
            @Part MultipartBody.Part tepTin,
            @Part("thuMuc") RequestBody thuMuc,
            @Part("tenTep") RequestBody tenTep
    );

    @POST("bang-tin")
    Call<ApiEnvelope<BangTinItemDto>> createPost(@Body BangTinCreateRequestDto request);
}
