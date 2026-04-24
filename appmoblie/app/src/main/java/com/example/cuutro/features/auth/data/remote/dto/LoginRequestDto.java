package com.example.cuutro.features.auth.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequestDto {

    @SerializedName("tenDangNhap")
    private final String tenDangNhap;

    @SerializedName("matKhau")
    private final String matKhau;

    public LoginRequestDto(String tenDangNhap, String matKhau) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }
}
