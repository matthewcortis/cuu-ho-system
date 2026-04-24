package com.example.cuutro.features.report.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoaiSuCoItemDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("ten")
    private String ten;

    @SerializedName("iconUrl")
    private String iconUrl;

    @SerializedName("moTa")
    private String moTa;

    @SerializedName("trangThai")
    private Boolean trangThai;

    public Long getId() {
        return id;
    }

    public String getTen() {
        return ten;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getMoTa() {
        return moTa;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }
}
