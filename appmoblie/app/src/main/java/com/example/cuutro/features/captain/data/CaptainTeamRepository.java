package com.example.cuutro.features.captain.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cuutro.core.network.NetworkCallExecutor;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.captain.data.model.CaptainTeamItem;
import com.example.cuutro.features.captain.data.model.CaptainTeamMemberItem;
import com.example.cuutro.features.captain.data.remote.CaptainTeamApiService;
import com.example.cuutro.features.captain.data.remote.dto.CaptainTeamDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CaptainTeamRepository {

    private static final String DEFAULT_TEAM_NAME = "Đội cứu hộ";
    private static final String DEFAULT_MEMBER_NAME = "Thành viên";
    private static final String DEFAULT_MEMBER_PHONE = "";

    private final CaptainTeamApiService captainTeamApiService;
    private final AuthRepository authRepository;
    private final NetworkCallExecutor networkCallExecutor;

    public CaptainTeamRepository(
            @NonNull CaptainTeamApiService captainTeamApiService,
            @NonNull AuthRepository authRepository,
            @NonNull NetworkCallExecutor networkCallExecutor
    ) {
        this.captainTeamApiService = captainTeamApiService;
        this.authRepository = authRepository;
        this.networkCallExecutor = networkCallExecutor;
    }

    public void getTeams(@NonNull ResultCallback<List<CaptainTeamItem>> callback) {
        if (!authRepository.hasActiveSession()) {
            callback.onError(new NetworkError(401, "Vui lòng đăng nhập để xem danh sách thành viên"));
            return;
        }
        networkCallExecutor.execute(
                captainTeamApiService.getTeams(),
                new ResultCallback<List<CaptainTeamDto>>() {
                    @Override
                    public void onSuccess(List<CaptainTeamDto> data) {
                        callback.onSuccess(mapTeams(data));
                    }

                    @Override
                    public void onError(@NonNull NetworkError error) {
                        if (error.isUnauthorized()) {
                            authRepository.clearSession();
                        }
                        callback.onError(error);
                    }
                }
        );
    }

    @NonNull
    private List<CaptainTeamItem> mapTeams(@Nullable List<CaptainTeamDto> teams) {
        if (teams == null || teams.isEmpty()) {
            return Collections.emptyList();
        }
        List<CaptainTeamItem> items = new ArrayList<>();
        for (CaptainTeamDto teamDto : teams) {
            if (teamDto == null) {
                continue;
            }
            items.add(new CaptainTeamItem(
                    teamDto.getId(),
                    fallbackIfBlank(teamDto.getTenDoiNhom(), DEFAULT_TEAM_NAME),
                    mapMembers(teamDto)
            ));
        }
        return items;
    }

    @NonNull
    private List<CaptainTeamMemberItem> mapMembers(@NonNull CaptainTeamDto teamDto) {
        Map<String, CaptainTeamMemberItem> dedupedMembers = new LinkedHashMap<>();
        addMember(dedupedMembers, teamDto.getDoiTruong());

        List<CaptainTeamDto.TeamMemberDto> thanhViens = teamDto.getThanhViens();
        if (thanhViens != null) {
            for (CaptainTeamDto.TeamMemberDto memberDto : thanhViens) {
                addMember(dedupedMembers, memberDto);
            }
        }

        List<CaptainTeamMemberItem> members = new ArrayList<>(dedupedMembers.values());
        Collections.sort(members, (left, right) -> {
            if (left.isLeader() && !right.isLeader()) {
                return -1;
            }
            if (!left.isLeader() && right.isLeader()) {
                return 1;
            }
            return left.getName().compareToIgnoreCase(right.getName());
        });
        return members;
    }

    private void addMember(
            @NonNull Map<String, CaptainTeamMemberItem> members,
            @Nullable CaptainTeamDto.TeamMemberDto memberDto
    ) {
        if (memberDto == null) {
            return;
        }
        CaptainTeamMemberItem mapped = new CaptainTeamMemberItem(
                memberDto.getTinhNguyenVienId(),
                fallbackIfBlank(memberDto.getTen(), DEFAULT_MEMBER_NAME),
                fallbackIfBlank(memberDto.getSdt(), DEFAULT_MEMBER_PHONE),
                trimToNull(memberDto.getAvatarUrl()),
                trimToNull(memberDto.getVaiTro())
        );
        String key = buildMemberKey(mapped);
        CaptainTeamMemberItem existing = members.get(key);
        if (existing == null || (!existing.isLeader() && mapped.isLeader())) {
            members.put(key, mapped);
        }
    }

    @NonNull
    private String buildMemberKey(@NonNull CaptainTeamMemberItem member) {
        if (member.getVolunteerId() != null) {
            return "id-" + member.getVolunteerId();
        }
        String normalizedName = fallbackIfBlank(member.getName(), "")
                .trim()
                .toLowerCase(Locale.ROOT);
        String normalizedPhone = fallbackIfBlank(member.getPhone(), "")
                .replaceAll("[^0-9+]", "");
        return "fallback-" + normalizedName + "-" + normalizedPhone;
    }

    @NonNull
    private String fallbackIfBlank(@Nullable String value, @NonNull String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
