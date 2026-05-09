package com.example.cuutro.features.captain.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CaptainTeamItem {

    @Nullable
    private final Long teamId;
    @NonNull
    private final String teamName;
    @NonNull
    private final List<CaptainTeamMemberItem> members;

    public CaptainTeamItem(
            @Nullable Long teamId,
            @NonNull String teamName,
            @NonNull List<CaptainTeamMemberItem> members
    ) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.members = members;
    }

    @Nullable
    public Long getTeamId() {
        return teamId;
    }

    @NonNull
    public String getTeamName() {
        return teamName;
    }

    @NonNull
    public List<CaptainTeamMemberItem> getMembers() {
        return members;
    }
}
