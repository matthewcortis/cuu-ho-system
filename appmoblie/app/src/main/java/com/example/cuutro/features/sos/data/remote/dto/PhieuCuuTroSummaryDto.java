package com.example.cuutro.features.sos.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class PhieuCuuTroSummaryDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("loaiSuCo")
    private LoaiSuCoDto loaiSuCo;

    @SerializedName("viTri")
    private ViTriDto viTri;

    @SerializedName("ghiChu")
    private String ghiChu;

    @SerializedName("trangThai")
    private String trangThai;

    public Long getId() {
        return id;
    }

    public LoaiSuCoDto getLoaiSuCo() {
        return loaiSuCo;
    }

    public ViTriDto getViTri() {
        return viTri;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
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
}
