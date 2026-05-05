package com.example.cuutro.features.chat.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ChatMessageResponseDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("sender")
    private SenderDto sender;

    @SerializedName("noiDung")
    private String noiDung;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("mediaUrl")
    private String mediaUrl;

    @SerializedName("mediaType")
    private String mediaType;

    @SerializedName("loaiTinNhan")
    private String loaiTinNhan;

    public Long getId() {
        return id;
    }

    public SenderDto getSender() {
        return sender;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getLoaiTinNhan() {
        return loaiTinNhan;
    }

    public static class SenderDto {

        @SerializedName("ten")
        private String ten;

        @SerializedName("taiKhoan")
        private TaiKhoanDto taiKhoan;

        public String getTen() {
            return ten;
        }

        public TaiKhoanDto getTaiKhoan() {
            return taiKhoan;
        }
    }

    public static class TaiKhoanDto {

        @SerializedName("id")
        private Long id;

        @SerializedName("tenDangNhap")
        private String tenDangNhap;

        public Long getId() {
            return id;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }
    }
}
