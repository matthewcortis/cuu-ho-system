package com.example.cuutro.features.sos.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EmergencyReportSupplyItem {

    private final String name;
    private final Integer quantity;
    private final String note;
    private final String iconUrl;

    public EmergencyReportSupplyItem(
            @NonNull String name,
            @Nullable Integer quantity,
            @NonNull String note,
            @Nullable String iconUrl
    ) {
        this.name = name;
        this.quantity = quantity;
        this.note = note;
        this.iconUrl = iconUrl;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public Integer getQuantity() {
        return quantity;
    }

    @NonNull
    public String getNote() {
        return note;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }
}
