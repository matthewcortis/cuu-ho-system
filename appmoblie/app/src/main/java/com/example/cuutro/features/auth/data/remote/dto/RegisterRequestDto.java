package com.example.cuutro.features.auth.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequestDto {

    @SerializedName("ten")
    private final String ten;

    @SerializedName("tenDangNhap")
    private final String tenDangNhap;

    @SerializedName("email")
    private final String email;

    @SerializedName("matKhau")
    private final String matKhau;

    public RegisterRequestDto(String ten, String tenDangNhap, String email, String matKhau) {
        this.ten = ten;
        this.tenDangNhap = tenDangNhap;
        this.email = email;
        this.matKhau = matKhau;
    }

    public String getTen() {
        return ten;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getEmail() {
        return email;
    }

    public String getMatKhau() {
        return matKhau;
    }
}
