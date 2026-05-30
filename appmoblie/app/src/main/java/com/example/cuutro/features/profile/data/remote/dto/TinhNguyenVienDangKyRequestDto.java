package com.example.cuutro.features.profile.data.remote.dto;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class TinhNguyenVienDangKyRequestDto {

    @SerializedName("nguoiDungId")
    private final String nguoiDungId;

    @SerializedName("thoiGian")
    private final String thoiGian;

    @SerializedName("ghiChu")
    private final String ghiChu;

    @SerializedName("coTheGiup")
    private final String coTheGiup;

    public TinhNguyenVienDangKyRequestDto(
            String nguoiDungId,
            @Nullable String thoiGian,
            @Nullable String ghiChu,
            @Nullable String coTheGiup
    ) {
        this.nguoiDungId = nguoiDungId;
        this.thoiGian = thoiGian;
        this.ghiChu = ghiChu;
        this.coTheGiup = coTheGiup;
    }
}
