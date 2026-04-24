package com.example.cuutro.features.report.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TaoPhieuCuuTroRequestDto {

    @SerializedName("loaiSuCoId")
    private final Long loaiSuCoId;

    @SerializedName("viTri")
    private final ViTriDto viTri;

    @SerializedName("nguoiGui")
    private final NguoiGuiDto nguoiGui;

    @SerializedName("ghiChu")
    private final String ghiChu;

    @SerializedName("chiTietCuuTro")
    private final List<ChiTietCuuTroDto> chiTietCuuTro;

    @SerializedName("tepTins")
    private final List<TepTinDto> tepTins;

    public TaoPhieuCuuTroRequestDto(
            Long loaiSuCoId,
            ViTriDto viTri,
            NguoiGuiDto nguoiGui,
            String ghiChu,
            List<ChiTietCuuTroDto> chiTietCuuTro,
            List<TepTinDto> tepTins
    ) {
        this.loaiSuCoId = loaiSuCoId;
        this.viTri = viTri;
        this.nguoiGui = nguoiGui;
        this.ghiChu = ghiChu;
        this.chiTietCuuTro = chiTietCuuTro == null ? new ArrayList<>() : chiTietCuuTro;
        this.tepTins = tepTins == null ? new ArrayList<>() : tepTins;
    }

    public static class ViTriDto {
        @SerializedName("diaChi")
        private final String diaChi;

        @SerializedName("lat")
        private final String lat;

        @SerializedName("longitude")
        private final String longitude;

        public ViTriDto(String diaChi, String lat, String longitude) {
            this.diaChi = diaChi;
            this.lat = lat;
            this.longitude = longitude;
        }
    }

    public static class NguoiGuiDto {
        @SerializedName("type")
        private final String type;

        @SerializedName("ten")
        private final String ten;

        @SerializedName("sdt")
        private final String sdt;

        public NguoiGuiDto(String type, String ten, String sdt) {
            this.type = type;
            this.ten = ten;
            this.sdt = sdt;
        }
    }

    public static class ChiTietCuuTroDto {
        @SerializedName("vatPhamId")
        private final Long vatPhamId;

        @SerializedName("soLuong")
        private final Integer soLuong;

        public ChiTietCuuTroDto(Long vatPhamId, Integer soLuong) {
            this.vatPhamId = vatPhamId;
            this.soLuong = soLuong;
        }
    }

    public static class TepTinDto {
        @SerializedName("tepTinId")
        private final Long tepTinId;

        @SerializedName("loai")
        private final String loai;

        @SerializedName("thuTu")
        private final Integer thuTu;

        public TepTinDto(Long tepTinId, String loai, Integer thuTu) {
            this.tepTinId = tepTinId;
            this.loai = loai;
            this.thuTu = thuTu;
        }
    }
}
