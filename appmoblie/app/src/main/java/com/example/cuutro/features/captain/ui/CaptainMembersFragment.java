package com.example.cuutro.features.captain.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.app.AppContainer;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.captain.data.CaptainTeamRepository;
import com.example.cuutro.features.captain.data.model.CaptainTeamItem;
import com.example.cuutro.features.captain.data.model.CaptainTeamMemberItem;
import com.example.cuutro.features.profile.data.ProfileRepository;
import com.example.cuutro.features.profile.data.model.UserProfileData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CaptainMembersFragment extends Fragment {

    private final List<CaptainTeamMemberItem> memberItems = new ArrayList<>();

    @Nullable
    private CaptainTeamRepository captainTeamRepository;
    @Nullable
    private ProfileRepository profileRepository;
    @Nullable
    private AuthRepository authRepository;
    @Nullable
    private CaptainTeamMemberAdapter adapter;
    @Nullable
    private TextView emptyStateView;
    @Nullable
    private TextView teamNameView;

    public CaptainMembersFragment() {
        super(R.layout.fragment_captain_members);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyStateView = view.findViewById(R.id.tvCaptainMembersEmpty);
        teamNameView = view.findViewById(R.id.tvCaptainMembersTeamName);

        MyApp app = (MyApp) requireActivity().getApplication();
        AppContainer appContainer = app.getAppContainer();
        if (appContainer != null) {
            captainTeamRepository = appContainer.getCaptainTeamRepository();
            profileRepository = appContainer.getProfileRepository();
            authRepository = appContainer.getAuthRepository();
        }

        setupRecyclerView(view);
        loadMembers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMembers();
    }

    private void setupRecyclerView(@NonNull View root) {
        RecyclerView recyclerView = root.findViewById(R.id.rvCaptainMembers);
        adapter = new CaptainTeamMemberAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMembers() {
        if (!isAdded()) {
            return;
        }
        if (authRepository == null || !authRepository.hasActiveSession()) {
            memberItems.clear();
            renderMembers(Collections.emptyList(), getString(R.string.captain_members_team_name_unknown));
            showEmptyState(getString(R.string.chat_login_required));
            return;
        }
        if (captainTeamRepository == null) {
            memberItems.clear();
            renderMembers(Collections.emptyList(), getString(R.string.captain_members_team_name_unknown));
            showEmptyState(getString(R.string.captain_members_empty));
            return;
        }

        showLoadingState();
        String fallbackUsername = trimToNull(authRepository.getCurrentUsername());
        if (profileRepository == null) {
            fetchMembersForCurrentCaptain(null, null, fallbackUsername);
            return;
        }

        profileRepository.getCurrentUserProfile(new ResultCallback<UserProfileData>() {
            @Override
            public void onSuccess(UserProfileData data) {
                String captainName = null;
                String captainPhone = null;
                String captainUsername = fallbackUsername;
                if (data != null) {
                    captainName = trimToNull(data.getHoTen());
                    captainPhone = trimToNull(data.getSoDienThoai());
                    if (captainUsername == null) {
                        captainUsername = trimToNull(data.getTenDangNhap());
                    }
                }
                fetchMembersForCurrentCaptain(captainName, captainPhone, captainUsername);
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                fetchMembersForCurrentCaptain(null, null, fallbackUsername);
            }
        });
    }

    private void fetchMembersForCurrentCaptain(
            @Nullable String captainName,
            @Nullable String captainPhone,
            @Nullable String captainUsername
    ) {
        if (!isAdded() || captainTeamRepository == null) {
            return;
        }
        captainTeamRepository.getTeams(new ResultCallback<List<CaptainTeamItem>>() {
            @Override
            public void onSuccess(List<CaptainTeamItem> data) {
                if (!isAdded()) {
                    return;
                }
                CaptainTeamItem currentTeam = resolveCurrentTeam(data, captainName, captainPhone, captainUsername);
                if (currentTeam == null) {
                    memberItems.clear();
                    renderMembers(Collections.emptyList(), getString(R.string.captain_members_team_name_unknown));
                    showEmptyState(getString(R.string.captain_members_not_found));
                    return;
                }
                memberItems.clear();
                memberItems.addAll(currentTeam.getMembers());
                String teamNameLabel = getString(
                        R.string.captain_members_team_name,
                        currentTeam.getTeamName()
                );
                renderMembers(memberItems, teamNameLabel);
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                memberItems.clear();
                renderMembers(Collections.emptyList(), getString(R.string.captain_members_team_name_unknown));
                showEmptyState(getString(R.string.captain_members_load_failed, error.getMessage()));
            }
        });
    }

    private void showLoadingState() {
        renderMembers(Collections.emptyList(), getString(R.string.captain_members_team_name_unknown));
        showEmptyState(getString(R.string.captain_members_loading));
    }

    private void renderMembers(
            @NonNull List<CaptainTeamMemberItem> members,
            @NonNull String teamNameLabel
    ) {
        if (adapter != null) {
            adapter.submitList(members);
        }
        if (teamNameView != null) {
            teamNameView.setText(teamNameLabel);
        }
        if (members.isEmpty()) {
            showEmptyState(getString(R.string.captain_members_empty));
            return;
        }
        hideEmptyState();
    }

    @Nullable
    private CaptainTeamItem resolveCurrentTeam(
            @Nullable List<CaptainTeamItem> teams,
            @Nullable String captainName,
            @Nullable String captainPhone,
            @Nullable String captainUsername
    ) {
        if (teams == null || teams.isEmpty()) {
            return null;
        }

        for (CaptainTeamItem team : teams) {
            if (team == null) {
                continue;
            }
            if (isCaptainLeaderOfTeam(team, captainName, captainPhone, captainUsername)) {
                return team;
            }
        }

        if (teams.size() == 1) {
            return teams.get(0);
        }
        return null;
    }

    private boolean isCaptainLeaderOfTeam(
            @NonNull CaptainTeamItem team,
            @Nullable String captainName,
            @Nullable String captainPhone,
            @Nullable String captainUsername
    ) {
        List<CaptainTeamMemberItem> members = team.getMembers();
        for (CaptainTeamMemberItem member : members) {
            if (member == null || !member.isLeader()) {
                continue;
            }
            if (isSameCaptain(member, captainName, captainPhone, captainUsername)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameCaptain(
            @NonNull CaptainTeamMemberItem member,
            @Nullable String captainName,
            @Nullable String captainPhone,
            @Nullable String captainUsername
    ) {
        String memberName = trimToNull(member.getName());
        String memberPhone = trimToNull(member.getPhone());

        String normalizedMemberPhone = normalizePhone(memberPhone);
        String normalizedCaptainPhone = normalizePhone(captainPhone);
        if (normalizedMemberPhone != null
                && normalizedCaptainPhone != null
                && normalizedMemberPhone.equals(normalizedCaptainPhone)) {
            return true;
        }

        if (isSameText(memberName, captainName)) {
            return true;
        }
        return isSameText(memberName, captainUsername);
    }

    @Nullable
    private String normalizePhone(@Nullable String rawPhone) {
        String phone = trimToNull(rawPhone);
        if (phone == null) {
            return null;
        }
        String normalized = phone.replaceAll("[^0-9+]", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isSameText(@Nullable String left, @Nullable String right) {
        String normalizedLeft = trimToNull(left);
        String normalizedRight = trimToNull(right);
        if (normalizedLeft == null || normalizedRight == null) {
            return false;
        }
        return normalizedLeft.toLowerCase(Locale.ROOT)
                .equals(normalizedRight.toLowerCase(Locale.ROOT));
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void showEmptyState(@NonNull String message) {
        if (emptyStateView == null) {
            return;
        }
        emptyStateView.setText(message);
        emptyStateView.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        if (emptyStateView == null) {
            return;
        }
        emptyStateView.setVisibility(View.GONE);
    }
}
