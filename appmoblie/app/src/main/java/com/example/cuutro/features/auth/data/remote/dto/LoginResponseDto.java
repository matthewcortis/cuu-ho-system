package com.example.cuutro.features.auth.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDto {

    @SerializedName("tokenType")
    private String tokenType;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("expiresAt")
    private String expiresAt;

    @SerializedName("taiKhoanId")
    private Long taiKhoanId;

    @SerializedName("tenDangNhap")
    private String tenDangNhap;

    @SerializedName("vaiTro")
    private String vaiTro;

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public Long getTaiKhoanId() {
        return taiKhoanId;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getVaiTro() {
        return vaiTro;
    }
}
