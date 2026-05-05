package com.example.cuutro.features.sos.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.sos.data.remote.dto.CapNhatTrangThaiPhieuRequestDto;
import com.example.cuutro.features.sos.data.remote.dto.PhieuCuuTroSummaryDto;
import com.example.cuutro.features.sos.data.remote.dto.TrangThaiPhieuResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Body;

public interface SosApiService {

    @GET("phieu-cuu-tro")
    Call<ApiEnvelope<List<PhieuCuuTroSummaryDto>>> getEmergencyReports();

    @GET("phieu-cuu-tro/{id}")
    Call<ApiEnvelope<PhieuCuuTroSummaryDto>> getEmergencyReportById(@Path("id") long reportId);

    @POST("phieu-cuu-tro/{id}/nhan-nhiem-vu")
    Call<ApiEnvelope<TrangThaiPhieuResponseDto>> nhanNhiemVu(@Path("id") long reportId);

    @POST("phieu-cuu-tro/{id}/tu-choi-nhiem-vu")
    Call<ApiEnvelope<TrangThaiPhieuResponseDto>> tuChoiNhiemVu(@Path("id") long reportId);

    @PUT("phieu-cuu-tro/{id}/trang-thai")
    Call<ApiEnvelope<TrangThaiPhieuResponseDto>> capNhatTrangThaiPhieu(
            @Path("id") long reportId,
            @Body CapNhatTrangThaiPhieuRequestDto request
    );
}
