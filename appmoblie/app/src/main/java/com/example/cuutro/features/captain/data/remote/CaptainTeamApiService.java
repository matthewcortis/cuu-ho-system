package com.example.cuutro.features.captain.data.remote;

import com.example.cuutro.core.network.ApiEnvelope;
import com.example.cuutro.features.captain.data.remote.dto.CaptainTeamDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CaptainTeamApiService {

    @GET("doi-nhom")
    Call<ApiEnvelope<List<CaptainTeamDto>>> getTeams();
}
