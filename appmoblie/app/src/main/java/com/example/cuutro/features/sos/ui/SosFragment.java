package com.example.cuutro.features.sos.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.sos.data.SosRepository;
import com.example.cuutro.features.sos.model.EmergencyReportItem;
import com.example.cuutro.features.splash.ui.NotificationScreenActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SosFragment extends Fragment {

    private final List<EmergencyReportItem> pendingReports = new ArrayList<>();
    private final List<EmergencyReportItem> completedReports = new ArrayList<>();

    private EmergencyReportAdapter adapter;
    private TextView reportedByYouView;
    private TextView reportedByOthersView;
    private TextView emptyStateView;
    private SosRepository sosRepository;
    private AuthRepository authRepository;

    private boolean isShowingReportedByYou = true;

    public SosFragment() {
        super(R.layout.fragment_sos);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportedByYouView = view.findViewById(R.id.btnReportedByYou);
        reportedByOthersView = view.findViewById(R.id.btnReportedByOthers);
        emptyStateView = view.findViewById(R.id.tvEmergencyEmpty);

        MyApp app = (MyApp) requireActivity().getApplication();
        sosRepository = app.getAppContainer().getSosRepository();
        authRepository = app.getAppContainer().getAuthRepository();

        setupRecyclerView(view);
        setupFilterActions();
        loadReportsFromBackend();
    }

    private void setupRecyclerView(@NonNull View root) {
        RecyclerView recyclerView = root.findViewById(R.id.rvEmergencyReports);
        adapter = new EmergencyReportAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterActions() {
        if (reportedByYouView != null) {
            reportedByYouView.setOnClickListener(v -> {
                if (!isShowingReportedByYou) {
                    isShowingReportedByYou = true;
                    renderCurrentTab();
                }
            });
        }
        if (reportedByOthersView != null) {
            reportedByOthersView.setOnClickListener(v -> {
                if (isShowingReportedByYou) {
                    if (!isUserLoggedIn()) {
                        startActivity(
                                NotificationScreenActivity.createUnauthorizedIntent(
                                        requireContext(),
                                        getString(R.string.auth_required_completed_reports_message)
                                )
                        );
                        return;
                    }
                    isShowingReportedByYou = false;
                    renderCurrentTab();
                }
            });
        }
    }

    private boolean isUserLoggedIn() {
        return authRepository != null && authRepository.hasActiveSession();
    }

    private void loadReportsFromBackend() {
        if (sosRepository == null) {
            pendingReports.clear();
            completedReports.clear();
            renderCurrentTab();
            return;
        }

        showLoadingState();
        sosRepository.getEmergencyReports(new ResultCallback<List<EmergencyReportItem>>() {
            @Override
            public void onSuccess(List<EmergencyReportItem> data) {
                if (!isAdded()) {
                    return;
                }
                applyRemoteReports(data);
                renderCurrentTab();
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                pendingReports.clear();
                completedReports.clear();
                renderCurrentTab();
                Toast.makeText(
                        requireContext(),
                        getString(R.string.sos_backend_sync_failed, error.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showLoadingState() {
        if (adapter != null) {
            adapter.submitList(Collections.emptyList());
        }
        if (emptyStateView != null) {
            emptyStateView.setText(R.string.sos_loading_reports);
            emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    private void applyRemoteReports(List<EmergencyReportItem> reports) {
        pendingReports.clear();
        completedReports.clear();

        if (reports == null) {
            return;
        }

        for (EmergencyReportItem item : reports) {
            if (item == null) {
                continue;
            }
            if (item.isCompleted()) {
                completedReports.add(item);
                continue;
            }
            pendingReports.add(item);
        }
    }

    private void renderCurrentTab() {
        if (adapter == null) {
            return;
        }

        List<EmergencyReportItem> activeList = isShowingReportedByYou
                ? pendingReports
                : completedReports;

        adapter.setShowDeleteAction(false);
        adapter.submitList(activeList);
        updateFilterUi();
        updateEmptyState(activeList.isEmpty());
    }

    private void updateFilterUi() {
        if (reportedByYouView == null || reportedByOthersView == null) {
            return;
        }
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.report_action_red);
        int unselectedColor = ContextCompat.getColor(requireContext(), R.color.report_label_inactive);

        if (isShowingReportedByYou) {
            reportedByYouView.setBackgroundResource(R.drawable.bg_sos_filter_selected);
            reportedByOthersView.setBackgroundResource(android.R.color.transparent);
            reportedByYouView.setTextColor(selectedColor);
            reportedByOthersView.setTextColor(unselectedColor);
        } else {
            reportedByYouView.setBackgroundResource(android.R.color.transparent);
            reportedByOthersView.setBackgroundResource(R.drawable.bg_sos_filter_selected);
            reportedByYouView.setTextColor(unselectedColor);
            reportedByOthersView.setTextColor(selectedColor);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyStateView == null) {
            return;
        }
        emptyStateView.setText(
                isShowingReportedByYou
                        ? R.string.sos_empty_reported_by_you
                        : R.string.sos_empty_reported_by_others
        );
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
