package com.example.cuutro.features.navigation.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.app.MyApp;
import com.example.cuutro.core.network.NetworkError;
import com.example.cuutro.core.network.ResultCallback;
import com.example.cuutro.features.auth.data.AuthRepository;
import com.example.cuutro.features.chat.ui.ChatsActivity;
import com.example.cuutro.features.sos.data.SosRepository;
import com.example.cuutro.features.sos.model.EmergencyReportItem;
import com.example.cuutro.features.sos.ui.EmergencyReportAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CaptainMemberMessagesFragment extends Fragment {

    private final List<EmergencyReportItem> memberMessageReports = new ArrayList<>();

    private SosRepository sosRepository;
    private AuthRepository authRepository;
    private EmergencyReportAdapter adapter;
    private TextView emptyStateView;
    private boolean isTaskActionRunning = false;

    public CaptainMemberMessagesFragment() {
        super(R.layout.fragment_captain_member_messages);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyApp app = (MyApp) requireActivity().getApplication();
        emptyStateView = view.findViewById(R.id.tvCaptainMemberMessagesEmpty);
        if (app.getAppContainer() == null) {
            showEmptyState(getString(R.string.chat_missing_dependency));
            return;
        }
        sosRepository = app.getAppContainer().getSosRepository();
        authRepository = app.getAppContainer().getAuthRepository();
        setupRecyclerView(view);
        loadMemberMessageReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMemberMessageReports();
    }

    private void setupRecyclerView(@NonNull View root) {
        RecyclerView recyclerView = root.findViewById(R.id.rvCaptainMemberMessages);
        adapter = new EmergencyReportAdapter(new EmergencyReportAdapter.Listener() {
            @Override
            public void onDeleteClicked(@NonNull EmergencyReportItem item, int position) {
                // No-op.
            }

            @Override
            public void onItemClicked(@NonNull EmergencyReportItem item, int position) {
                openReportChat(item);
            }
        });
        adapter.setShowDeleteAction(false);
        adapter.setShowTaskActions(true);
        adapter.setTaskActionListener(new EmergencyReportAdapter.TaskActionListener() {
            @Override
            public void onAcceptTask(@NonNull EmergencyReportItem item, int position) {
                thucHienNhiemVuAction(item, true);
            }

            @Override
            public void onRejectTask(@NonNull EmergencyReportItem item, int position) {
                thucHienNhiemVuAction(item, false);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMemberMessageReports() {
        if (!isAdded()) {
            return;
        }
        if (authRepository == null || !authRepository.hasActiveSession()) {
            memberMessageReports.clear();
            renderReports(Collections.emptyList());
            showEmptyState(getString(R.string.chat_login_required));
            return;
        }
        if (sosRepository == null) {
            memberMessageReports.clear();
            renderReports(Collections.emptyList());
            showEmptyState(getString(R.string.captain_member_messages_empty));
            return;
        }

        showLoadingState();
        sosRepository.getEmergencyReports(new ResultCallback<List<EmergencyReportItem>>() {
            @Override
            public void onSuccess(List<EmergencyReportItem> data) {
                if (!isAdded()) {
                    return;
                }
                applyMessageReports(data);
                renderReports(memberMessageReports);
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                memberMessageReports.clear();
                renderReports(Collections.emptyList());
                Toast.makeText(
                        requireContext(),
                        getString(R.string.captain_member_messages_load_failed, error.getMessage()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void showLoadingState() {
        if (adapter != null) {
            adapter.submitList(Collections.emptyList());
        }
        showEmptyState(getString(R.string.captain_member_messages_loading));
    }

    private void applyMessageReports(List<EmergencyReportItem> reports) {
        memberMessageReports.clear();
        if (reports == null) {
            return;
        }
        for (EmergencyReportItem item : reports) {
            if (item == null || item.isCompleted()) {
                continue;
            }
            memberMessageReports.add(item);
        }
    }

    private void renderReports(@NonNull List<EmergencyReportItem> reports) {
        if (adapter != null) {
            adapter.submitList(reports);
        }
        if (reports.isEmpty()) {
            showEmptyState(getString(R.string.captain_member_messages_empty));
            return;
        }
        hideEmptyState();
    }

    private void thucHienNhiemVuAction(@NonNull EmergencyReportItem reportItem, boolean isNhanNhiemVu) {
        if (!isAdded() || sosRepository == null) {
            return;
        }
        if (isTaskActionRunning) {
            return;
        }
        long reportId = parseReportId(reportItem.getId());
        if (reportId <= 0L) {
            Toast.makeText(requireContext(), R.string.chat_invalid_report_id, Toast.LENGTH_SHORT).show();
            return;
        }

        isTaskActionRunning = true;
        ResultCallback<String> callback = new ResultCallback<String>() {
            @Override
            public void onSuccess(String status) {
                if (!isAdded()) {
                    return;
                }
                isTaskActionRunning = false;
                int successMessageRes = isNhanNhiemVu
                        ? R.string.captain_task_accept_success
                        : R.string.captain_task_reject_success;
                Toast.makeText(requireContext(), successMessageRes, Toast.LENGTH_SHORT).show();
                loadMemberMessageReports();
            }

            @Override
            public void onError(@NonNull NetworkError error) {
                if (!isAdded()) {
                    return;
                }
                isTaskActionRunning = false;
                int errorMessageRes = isNhanNhiemVu
                        ? R.string.captain_task_accept_failed
                        : R.string.captain_task_reject_failed;
                Toast.makeText(
                        requireContext(),
                        getString(errorMessageRes, error.getMessage()),
                        Toast.LENGTH_SHORT
                ).show();
            }
        };

        if (isNhanNhiemVu) {
            sosRepository.nhanNhiemVu(reportId, callback);
        } else {
            sosRepository.tuChoiNhiemVu(reportId, callback);
        }
    }

    private void openReportChat(@NonNull EmergencyReportItem reportItem) {
        long reportId = parseReportId(reportItem.getId());
        if (reportId <= 0L) {
            Toast.makeText(requireContext(), R.string.chat_invalid_report_id, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(ChatsActivity.createIntent(requireContext(), reportId));
    }

    private long parseReportId(String rawReportId) {
        if (rawReportId == null) {
            return -1L;
        }
        String normalized = rawReportId.trim();
        if (normalized.isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ignored) {
            return -1L;
        }
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
