package com.example.cuutro.features.sos.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class TrangThaiPhieuResponseDto {

    @SerializedName("phieuId")
    private Long phieuId;

    @SerializedName("trangThai")
    private String trangThai;

    public Long getPhieuId() {
        return phieuId;
    }

    public String getTrangThai() {
        return trangThai;
    }
}
