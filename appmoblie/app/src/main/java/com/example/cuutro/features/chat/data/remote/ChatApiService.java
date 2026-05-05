package com.example.cuutro.features.chat.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.chat.data.remote.dto.ChatMessageResponseDto;
import com.example.cuutro.features.chat.data.remote.dto.ChatSendMessageRequestDto;
import com.example.cuutro.features.chat.data.remote.dto.ChatUploadFileResponseDto;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ChatApiService {

    @GET("phieu-cuu-tro/{id}/tin-nhan")
    Call<ApiEnvelope<List<ChatMessageResponseDto>>> getConversationMessages(@Path("id") long reportId);

    @POST("phieu-cuu-tro/{id}/tin-nhan")
    Call<ApiEnvelope<ChatMessageResponseDto>> sendMessage(
            @Path("id") long reportId,
            @Body ChatSendMessageRequestDto request
    );

    @Multipart
    @POST("tep-tin/upload")
    Call<ApiEnvelope<ChatUploadFileResponseDto>> uploadAttachment(
            @Part MultipartBody.Part tepTin,
            @Part("thuMuc") RequestBody thuMuc,
            @Part("tenTep") RequestBody tenTep
    );
}
