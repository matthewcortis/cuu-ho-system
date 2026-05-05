package com.example.cuutro.features.sos.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public class EmergencyReportDetail {

    private final String id;
    private final String title;
    private final String location;
    private final String description;
    private final String status;
    private final String statusLabel;
    private final boolean completed;
    private final String reporterName;
    private final String reporterPhone;
    private final String createdAt;
    private final String incidentIconUrl;
    private final List<String> imageUrls;
    private final List<EmergencyReportSupplyItem> supplyItems;

    public EmergencyReportDetail(
            @NonNull String id,
            @NonNull String title,
            @NonNull String location,
            @NonNull String description,
            @Nullable String status,
            @NonNull String statusLabel,
            boolean completed,
            @NonNull String reporterName,
            @NonNull String reporterPhone,
            @Nullable String createdAt,
            @Nullable String incidentIconUrl,
            @NonNull List<String> imageUrls,
            @NonNull List<EmergencyReportSupplyItem> supplyItems
    ) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.description = description;
        this.status = status;
        this.statusLabel = statusLabel;
        this.completed = completed;
        this.reporterName = reporterName;
        this.reporterPhone = reporterPhone;
        this.createdAt = createdAt;
        this.incidentIconUrl = incidentIconUrl;
        this.imageUrls = Collections.unmodifiableList(imageUrls);
        this.supplyItems = Collections.unmodifiableList(supplyItems);
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getLocation() {
        return location;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getStatus() {
        return status;
    }

    @NonNull
    public String getStatusLabel() {
        return statusLabel;
    }

    public boolean isCompleted() {
        return completed;
    }

    @NonNull
    public String getReporterName() {
        return reporterName;
    }

    @NonNull
    public String getReporterPhone() {
        return reporterPhone;
    }

    @Nullable
    public String getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public String getIncidentIconUrl() {
        return incidentIconUrl;
    }

    @NonNull
    public List<String> getImageUrls() {
        return imageUrls;
    }

    @NonNull
    public List<EmergencyReportSupplyItem> getSupplyItems() {
        return supplyItems;
    }
}
