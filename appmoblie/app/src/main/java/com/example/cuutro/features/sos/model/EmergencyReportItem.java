package com.example.cuutro.features.sos.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class EmergencyReportItem {

    private final String id;
    private final String location;
    private final String title;
    private final String description;
    private final int iconResId;
    private final boolean completed;
    private final String status;
    private final String statusLabel;

    public EmergencyReportItem(
            @NonNull String id,
            @NonNull String location,
            @NonNull String title,
            @NonNull String description,
            @DrawableRes int iconResId
    ) {
        this(id, location, title, description, iconResId, false, null, null);
    }

    public EmergencyReportItem(
            @NonNull String id,
            @NonNull String location,
            @NonNull String title,
            @NonNull String description,
            @DrawableRes int iconResId,
            boolean completed
    ) {
        this(id, location, title, description, iconResId, completed, null, null);
    }

    public EmergencyReportItem(
            @NonNull String id,
            @NonNull String location,
            @NonNull String title,
            @NonNull String description,
            @DrawableRes int iconResId,
            boolean completed,
            String status
    ) {
        this(id, location, title, description, iconResId, completed, status, null);
    }

    public EmergencyReportItem(
            @NonNull String id,
            @NonNull String location,
            @NonNull String title,
            @NonNull String description,
            @DrawableRes int iconResId,
            boolean completed,
            String status,
            String statusLabel
    ) {
        this.id = id;
        this.location = location;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.completed = completed;
        this.status = status;
        this.statusLabel = statusLabel;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getLocation() {
        return location;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @DrawableRes
    public int getIconResId() {
        return iconResId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}
