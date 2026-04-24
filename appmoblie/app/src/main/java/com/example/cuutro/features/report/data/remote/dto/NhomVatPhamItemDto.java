package com.example.cuutro.features.report.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NhomVatPhamItemDto {

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

    public static class LoaiSuCoDto {
        @SerializedName("id")
        private Long id;

        public Long getId() {
            return id;
        }
    }
}
