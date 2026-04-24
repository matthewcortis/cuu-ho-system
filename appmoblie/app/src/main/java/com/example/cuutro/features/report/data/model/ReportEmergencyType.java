package com.example.cuutro.features.report.data.model;

import androidx.annotation.Nullable;

public class ReportEmergencyType {

    @Nullable
    private final Long id;
    private final String label;
    @Nullable
    private final String iconUrl;
    private final int iconResId;

    public ReportEmergencyType(@Nullable Long id, String label, @Nullable String iconUrl, int iconResId) {
        this.id = id;
        this.label = label;
        this.iconUrl = iconUrl;
        this.iconResId = iconResId;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public int getIconResId() {
        return iconResId;
    }
}
