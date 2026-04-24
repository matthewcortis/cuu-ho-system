package com.example.cuutro.features.profile.data.model;

import androidx.annotation.Nullable;

public class UserProfileData {

    private final String nguoiDungId;
    private final Long taiKhoanId;
    private final String tenDangNhap;
    private final String hoTen;
    private final String email;
    private final String soDienThoai;
    private final String diaChi;
    private final String viDo;
    private final String kinhDo;
    private final String avatarUrl;

    public UserProfileData(
            @Nullable String nguoiDungId,
            @Nullable Long taiKhoanId,
            @Nullable String tenDangNhap,
            @Nullable String hoTen,
            @Nullable String email,
            @Nullable String soDienThoai,
            @Nullable String diaChi,
            @Nullable String viDo,
            @Nullable String kinhDo,
            @Nullable String avatarUrl
    ) {
        this.nguoiDungId = nguoiDungId;
        this.taiKhoanId = taiKhoanId;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.avatarUrl = avatarUrl;
    }

    @Nullable
    public String getNguoiDungId() {
        return nguoiDungId;
    }

    @Nullable
    public Long getTaiKhoanId() {
        return taiKhoanId;
    }

    @Nullable
    public String getTenDangNhap() {
        return tenDangNhap;
    }

    @Nullable
    public String getHoTen() {
        return hoTen;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getSoDienThoai() {
        return soDienThoai;
    }

    @Nullable
    public String getDiaChi() {
        return diaChi;
    }

    @Nullable
    public String getViDo() {
        return viDo;
    }

    @Nullable
    public String getKinhDo() {
        return kinhDo;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }
}
