package com.example.cuutro.features.report.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.report.data.remote.dto.LoaiSuCoFilterPageDto;
import com.example.cuutro.features.report.data.remote.dto.LoaiSuCoFilterRequestDto;
import com.example.cuutro.features.report.data.remote.dto.NhomVatPhamItemDto;
import com.example.cuutro.features.report.data.remote.dto.TaoPhieuCuuTroRequestDto;
import com.example.cuutro.features.report.data.remote.dto.TaoPhieuCuuTroResponseDto;
import com.example.cuutro.features.report.data.remote.dto.TepTinUploadResponseDto;
import com.example.cuutro.features.report.data.remote.dto.VatPhamItemDto;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ReportApiService {

    @POST("loai-su-co/filter")
    Call<ApiEnvelope<LoaiSuCoFilterPageDto>> filterLoaiSuCo(@Body LoaiSuCoFilterRequestDto request);

    @GET("vat-pham")
    Call<ApiEnvelope<List<VatPhamItemDto>>> getVatPhamList();

    @GET("nhom-vat-pham")
    Call<ApiEnvelope<List<NhomVatPhamItemDto>>> getNhomVatPhamList();

    @Multipart
    @POST("tep-tin/upload")
    Call<ApiEnvelope<TepTinUploadResponseDto>> uploadAttachment(
            @Part MultipartBody.Part tepTin,
            @Part("thuMuc") RequestBody thuMuc,
            @Part("tenTep") RequestBody tenTep
    );

    @POST("phieu-cuu-tro")
    Call<ApiEnvelope<TaoPhieuCuuTroResponseDto>> createRescueTicket(@Body TaoPhieuCuuTroRequestDto request);
}
