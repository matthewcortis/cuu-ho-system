package com.example.cuutro.features.report.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoaiSuCoFilterPageDto {

    @SerializedName("content")
    private List<LoaiSuCoItemDto> content;

    public List<LoaiSuCoItemDto> getContent() {
        return content;
    }
}
