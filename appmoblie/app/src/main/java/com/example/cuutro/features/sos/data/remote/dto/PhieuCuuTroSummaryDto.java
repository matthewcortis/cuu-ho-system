package com.example.cuutro.features.sos.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhieuCuuTroSummaryDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("loaiSuCo")
    private LoaiSuCoDto loaiSuCo;

    @SerializedName("viTri")
    private ViTriDto viTri;

    @SerializedName("nguoiGui")
    private NguoiGuiDto nguoiGui;

    @SerializedName("tepTins")
    private List<PhieuCuuTroTepTinDto> tepTins;

    @SerializedName("chiTietCuuTro")
    private List<PhieuCuuTroChiTietDto> chiTietCuuTro;

    @SerializedName("ghiChu")
    private String ghiChu;

    @SerializedName("trangThai")
    private String trangThai;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() {
        return id;
    }

    public LoaiSuCoDto getLoaiSuCo() {
        return loaiSuCo;
    }

    public ViTriDto getViTri() {
        return viTri;
    }

    public NguoiGuiDto getNguoiGui() {
        return nguoiGui;
    }

    public List<PhieuCuuTroTepTinDto> getTepTins() {
        return tepTins;
    }

    public List<PhieuCuuTroChiTietDto> getChiTietCuuTro() {
        return chiTietCuuTro;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public static class LoaiSuCoDto {

        @SerializedName("id")
        private Long id;

        @SerializedName("ten")
        private String ten;

        @SerializedName("iconUrl")
        private String iconUrl;

        public Long getId() {
            return id;
        }

        public String getTen() {
            return ten;
        }

        public String getIconUrl() {
            return iconUrl;
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

    public static class NguoiGuiDto {

        @SerializedName("ten")
        private String ten;

        @SerializedName("sdt")
        private String sdt;

        public String getTen() {
            return ten;
        }

        public String getSdt() {
            return sdt;
        }
    }

    public static class PhieuCuuTroTepTinDto {

        @SerializedName("tepTin")
        private TepTinDto tepTin;

        public TepTinDto getTepTin() {
            return tepTin;
        }
    }

    public static class TepTinDto {

        @SerializedName("duongDan")
        private String duongDan;

        @SerializedName("loaiTepTin")
        private String loaiTepTin;

        public String getDuongDan() {
            return duongDan;
        }

        public String getLoaiTepTin() {
            return loaiTepTin;
        }
    }

    public static class PhieuCuuTroChiTietDto {

        @SerializedName("tenVatPham")
        private String tenVatPham;

        @SerializedName("iconUrl")
        private String iconUrl;

        @SerializedName("soLuong")
        private Integer soLuong;

        @SerializedName("ghiChu")
        private String ghiChu;

        public String getTenVatPham() {
            return tenVatPham;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public Integer getSoLuong() {
            return soLuong;
        }

        public String getGhiChu() {
            return ghiChu;
        }
    }
}
