package com.example.cuutro.features.report.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VatPhamItemDto {

    @SerializedName("id")
    private Long id;

    @SerializedName("tenVatPham")
    private String tenVatPham;

    @SerializedName("soLuong")
    private Integer soLuong;

    @SerializedName("trangThai")
    private Boolean trangThai;

    @SerializedName("donVi")
    private DonViDto donVi;

    @SerializedName("nhomVatPham")
    private NhomVatPhamDto nhomVatPham;

    @SerializedName("nhomVatPhams")
    private List<NhomVatPhamDto> nhomVatPhams;

    @SerializedName("tepTin")
    private TepTinDto tepTin;

    public Long getId() {
        return id;
    }

    public String getTenVatPham() {
        return tenVatPham;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public DonViDto getDonVi() {
        return donVi;
    }

    public NhomVatPhamDto getNhomVatPham() {
        return nhomVatPham;
    }

    public List<NhomVatPhamDto> getNhomVatPhams() {
        return nhomVatPhams;
    }

    public TepTinDto getTepTin() {
        return tepTin;
    }

    public static class DonViDto {
        @SerializedName("id")
        private Long id;

        @SerializedName("ten")
        private String ten;

        public Long getId() {
            return id;
        }

        public String getTen() {
            return ten;
        }
    }

    public static class NhomVatPhamDto {
        @SerializedName("id")
        private Long id;

        @SerializedName("ten")
        private String ten;

        @SerializedName("loaiSuCo")
        private LoaiSuCoDto loaiSuCo;

        @SerializedName("loaiSuCos")
        private List<LoaiSuCoDto> loaiSuCos;

        public Long getId() {
            return id;
        }

        public String getTen() {
            return ten;
        }

        public LoaiSuCoDto getLoaiSuCo() {
            return loaiSuCo;
        }

        public List<LoaiSuCoDto> getLoaiSuCos() {
            return loaiSuCos;
        }
    }

    public static class LoaiSuCoDto {
        @SerializedName("id")
        private Long id;

        public Long getId() {
            return id;
        }
    }

    public static class TepTinDto {
        @SerializedName("id")
        private Long id;

        @SerializedName("duongDan")
        private String duongDan;

        public Long getId() {
            return id;
        }

        public String getDuongDan() {
            return duongDan;
        }
    }
}
