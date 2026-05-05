package com.example.cuutro.features.chat.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ChatSendMessageRequestDto {

    @SerializedName("noiDung")
    private final String noiDung;

    @SerializedName("tepTinId")
    private final Long tepTinId;

    @SerializedName("viTriId")
    private final Long viTriId;

    @SerializedName("viTri")
    private final ViTriInputDto viTri;

    public ChatSendMessageRequestDto(
            String noiDung,
            Long tepTinId,
            Long viTriId,
            ViTriInputDto viTri
    ) {
        this.noiDung = noiDung;
        this.tepTinId = tepTinId;
        this.viTriId = viTriId;
        this.viTri = viTri;
    }

    public static class ViTriInputDto {

        @SerializedName("diaChi")
        private final String diaChi;

        @SerializedName("lat")
        private final String lat;

        @SerializedName("longitude")
        private final String longitude;

        public ViTriInputDto(String diaChi, String lat, String longitude) {
            this.diaChi = diaChi;
            this.lat = lat;
            this.longitude = longitude;
        }
    }
}
