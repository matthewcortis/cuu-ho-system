package com.example.cuutro.features.sos.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CapNhatTrangThaiPhieuRequestDto {

    @SerializedName("trangThai")
    private final String trangThai;

    public CapNhatTrangThaiPhieuRequestDto(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTrangThai() {
        return trangThai;
    }
}
