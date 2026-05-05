package com.example.cuutro.features.chat.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ChatUploadFileResponseDto {

    @SerializedName("id")
    private Long id;

    public Long getId() {
        return id;
    }
}
