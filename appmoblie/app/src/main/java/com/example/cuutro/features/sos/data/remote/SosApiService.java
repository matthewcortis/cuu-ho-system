package com.example.cuutro.features.sos.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.sos.data.remote.dto.PhieuCuuTroSummaryDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SosApiService {

    @GET("phieu-cuu-tro")
    Call<ApiEnvelope<List<PhieuCuuTroSummaryDto>>> getEmergencyReports();
}
