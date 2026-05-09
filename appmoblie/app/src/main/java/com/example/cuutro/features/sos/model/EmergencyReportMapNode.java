package com.example.cuutro.features.sos.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EmergencyReportMapNode {

    @NonNull
    private final String reportId;
    private final double latitude;
    private final double longitude;
    @Nullable
    private final String status;

    public EmergencyReportMapNode(
            @NonNull String reportId,
            double latitude,
            double longitude,
            @Nullable String status
    ) {
        this.reportId = reportId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    @NonNull
    public String getReportId() {
        return reportId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getStatus() {
        return status;
    }
}
