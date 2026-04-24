package com.example.cuutro.features.profile.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class NguoiDungDoiMatKhauRequestDto {

    @SerializedName("matKhauHienTai")
    private final String matKhauHienTai;

    @SerializedName("matKhauMoi")
    private final String matKhauMoi;

    public NguoiDungDoiMatKhauRequestDto(String matKhauHienTai, String matKhauMoi) {
        this.matKhauHienTai = matKhauHienTai;
        this.matKhauMoi = matKhauMoi;
    }
}
