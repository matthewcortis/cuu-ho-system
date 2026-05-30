package com.example.cuutro.features.auth.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequestDto {

    @SerializedName("email")
    private final String email;

    @SerializedName("matKhauMoi")
    private final String matKhauMoi;

    public ForgotPasswordRequestDto(String email, String matKhauMoi) {
        this.email = email;
        this.matKhauMoi = matKhauMoi;
    }

    public String getEmail() {
        return email;
    }

    public String getMatKhauMoi() {
        return matKhauMoi;
    }
}
