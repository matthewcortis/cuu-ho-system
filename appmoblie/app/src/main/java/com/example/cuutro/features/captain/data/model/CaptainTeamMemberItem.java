package com.example.cuutro.features.captain.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class CaptainTeamMemberItem {

    @Nullable
    private final Long volunteerId;
    @NonNull
    private final String name;
    @NonNull
    private final String phone;
    @Nullable
    private final String avatarUrl;
    @Nullable
    private final String role;

    public CaptainTeamMemberItem(
            @Nullable Long volunteerId,
            @NonNull String name,
            @NonNull String phone,
            @Nullable String avatarUrl,
            @Nullable String role
    ) {
        this.volunteerId = volunteerId;
        this.name = name;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    @Nullable
    public Long getVolunteerId() {
        return volunteerId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getPhone() {
        return phone;
    }

    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Nullable
    public String getRole() {
        return role;
    }

    public boolean isLeader() {
        if (role == null) {
            return false;
        }
        return "truong_nhom".equals(role.trim().toLowerCase(Locale.ROOT));
    }
}
