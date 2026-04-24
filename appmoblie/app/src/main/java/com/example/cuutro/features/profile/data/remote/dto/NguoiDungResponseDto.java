package com.example.cuutro.features.profile.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class NguoiDungResponseDto {

    @SerializedName("id")
    private String id;

    @SerializedName("taiKhoan")
    private TaiKhoanDto taiKhoan;

    @SerializedName("ten")
    private String ten;

    @SerializedName("sdt")
    private String sdt;

    @SerializedName("viTri")
    private ViTriDto viTri;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    public String getId() {
        return id;
    }

    public TaiKhoanDto getTaiKhoan() {
        return taiKhoan;
    }

    public String getTen() {
        return ten;
    }

    public String getSdt() {
        return sdt;
    }

    public ViTriDto getViTri() {
        return viTri;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public static class TaiKhoanDto {

        @SerializedName("id")
        private Long id;

        @SerializedName("email")
        private String email;

        @SerializedName("tenDangNhap")
        private String tenDangNhap;

        @SerializedName("vaiTro")
        private String vaiTro;

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }

        public String getVaiTro() {
            return vaiTro;
        }
    }

    public static class ViTriDto {

        @SerializedName("id")
        private Long id;

        @SerializedName("lat")
        private String lat;

        @SerializedName("longitude")
        private String longitude;

        @SerializedName("diaChi")
        private String diaChi;

        public Long getId() {
            return id;
        }

        public String getLat() {
            return lat;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getDiaChi() {
            return diaChi;
        }
    }
}
