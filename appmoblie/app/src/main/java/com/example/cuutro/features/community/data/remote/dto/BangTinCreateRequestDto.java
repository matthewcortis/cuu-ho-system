package com.example.cuutro.features.community.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class BangTinCreateRequestDto {

    @SerializedName("tieuDe")
    private final String tieuDe;

    @SerializedName("noiDung")
    private final String noiDung;

    @SerializedName("tepTinId")
    private final Long tepTinId;

    @SerializedName("viTri")
    private final ViTriInputDto viTri;

    public BangTinCreateRequestDto(
            String tieuDe,
            String noiDung,
            @Nullable Long tepTinId,
            @Nullable ViTriInputDto viTri
    ) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.tepTinId = tepTinId;
        this.viTri = viTri;
    }

    public static class ViTriInputDto {

        @SerializedName("diaChi")
        private final String diaChi;

        @SerializedName("lat")
        private final String lat;

        @SerializedName("longitude")
        private final String longitude;

        public ViTriInputDto(
                String diaChi,
                @Nullable String lat,
                @Nullable String longitude
        ) {
            this.diaChi = diaChi;
            this.lat = lat;
            this.longitude = longitude;
        }
    }
}
