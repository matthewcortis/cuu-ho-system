package com.example.cuutro.features.auth.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterResponseDto {

    @SerializedName("taiKhoanId")
    private Long taiKhoanId;

    @SerializedName("tenDangNhap")
    private String tenDangNhap;

    @SerializedName("email")
    private String email;

    public Long getTaiKhoanId() {
        return taiKhoanId;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getEmail() {
        return email;
    }
}
