package com.example.cuutro.features.profile.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class NguoiDungUpsertRequestDto {

    @SerializedName("taiKhoanId")
    private final Long taiKhoanId;

    @SerializedName("ten")
    private final String ten;

    @SerializedName("email")
    private final String email;

    @SerializedName("sdt")
    private final String sdt;

    @SerializedName("viTri")
    private final ViTriInputDto viTri;

    @SerializedName("avatarUrl")
    private final String avatarUrl;

    public NguoiDungUpsertRequestDto(
            Long taiKhoanId,
            String ten,
            String email,
            String sdt,
            @Nullable ViTriInputDto viTri,
            @Nullable String avatarUrl
    ) {
        this.taiKhoanId = taiKhoanId;
        this.ten = ten;
        this.email = email;
        this.sdt = sdt;
        this.viTri = viTri;
        this.avatarUrl = avatarUrl;
    }

    public static class ViTriInputDto {

        @SerializedName("diaChi")
        private final String diaChi;

        @SerializedName("lat")
        private final String lat;

        @SerializedName("longitude")
        private final String longitude;

        public ViTriInputDto(String diaChi, @Nullable String lat, @Nullable String longitude) {
            this.diaChi = diaChi;
            this.lat = lat;
            this.longitude = longitude;
        }
    }
}
