package com.example.cuutro.core.network;

import com.google.gson.annotations.SerializedName;

public class ApiEnvelope<T> {

    @SerializedName("status")
    private int status;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    @SerializedName("path")
    private String path;

    @SerializedName("timestamp")
    private String timestamp;

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
