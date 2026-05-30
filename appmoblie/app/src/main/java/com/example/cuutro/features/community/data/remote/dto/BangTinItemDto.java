package com.example.cuutro.features.community.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class BangTinItemDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("tieuDe")
    private String tieuDe;

    @SerializedName("noiDung")
    private String noiDung;

    @SerializedName("tepTin")
    private TepTinDto tepTin;

    @SerializedName("viTri")
    private ViTriDto viTri;

    @SerializedName("nguoiDung")
    private NguoiDungDto nguoiDung;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() {
        return id;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public TepTinDto getTepTin() {
        return tepTin;
    }

    public ViTriDto getViTri() {
        return viTri;
    }

    public NguoiDungDto getNguoiDung() {
        return nguoiDung;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static class TepTinDto {

        @SerializedName("duongDan")
        private String duongDan;

        public String getDuongDan() {
            return duongDan;
        }
    }

    public static class ViTriDto {

        @SerializedName("diaChi")
        private String diaChi;

        public String getDiaChi() {
            return diaChi;
        }
    }

    public static class NguoiDungDto {

        @SerializedName("ten")
        private String ten;

        @SerializedName("avatarUrl")
        private String avatarUrl;

        public String getTen() {
            return ten;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }
}
