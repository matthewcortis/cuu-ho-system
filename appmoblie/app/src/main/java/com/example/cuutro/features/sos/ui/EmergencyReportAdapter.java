package com.example.cuutro.features.sos.ui;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.features.report.ui.controller.ReportBitmapLoader;
import com.example.cuutro.features.sos.model.EmergencyReportItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmergencyReportAdapter extends RecyclerView.Adapter<EmergencyReportAdapter.EmergencyReportViewHolder> {

    public interface Listener {
        void onDeleteClicked(@NonNull EmergencyReportItem item, int position);

        void onItemClicked(@NonNull EmergencyReportItem item, int position);
    }

    public interface TaskActionListener {
        void onAcceptTask(@NonNull EmergencyReportItem item, int position);

        void onRejectTask(@NonNull EmergencyReportItem item, int position);
    }

    public interface StatusUpdateListener {
        void onUpdateTaskStatus(@NonNull EmergencyReportItem item, int position, @NonNull String statusCode);
    }

    private final List<EmergencyReportItem> items = new ArrayList<>();
    private final Map<String, String> selectedStatusByReportId = new HashMap<>();
    @Nullable
    private final Listener listener;
    @Nullable
    private TaskActionListener taskActionListener;
    @Nullable
    private StatusUpdateListener statusUpdateListener;
    private boolean showDeleteAction = true;
    private boolean showTaskActions = false;
    private boolean showTaskStatusUpdater = false;
    private final ReportBitmapLoader bitmapLoader = new ReportBitmapLoader();

    public EmergencyReportAdapter() {
        this(null);
    }

    public EmergencyReportAdapter(@Nullable Listener listener) {
        this.listener = listener;
    }

    public void submitList(@NonNull List<EmergencyReportItem> reports) {
        items.clear();
        items.addAll(reports);
        notifyDataSetChanged();
    }

    public void setShowDeleteAction(boolean showDeleteAction) {
        this.showDeleteAction = showDeleteAction;
    }

    public void setTaskActionListener(@Nullable TaskActionListener taskActionListener) {
        this.taskActionListener = taskActionListener;
    }

    public void setShowTaskActions(boolean showTaskActions) {
        this.showTaskActions = showTaskActions;
    }

    public void setShowTaskStatusUpdater(boolean showTaskStatusUpdater) {
        this.showTaskStatusUpdater = showTaskStatusUpdater;
    }

    public void setStatusUpdateListener(@Nullable StatusUpdateListener statusUpdateListener) {
        this.statusUpdateListener = statusUpdateListener;
    }

    public void release() {
        bitmapLoader.shutdown();
    }

    @NonNull
    @Override
    public EmergencyReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_emergency_report, parent, false);
        return new EmergencyReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyReportViewHolder holder, int position) {
        EmergencyReportItem item = items.get(position);
        holder.bind(item, showDeleteAction, showTaskActions, showTaskStatusUpdater, bitmapLoader);
        holder.bindStatusUpdater(
                item,
                selectedStatusByReportId.get(item.getId()),
                statusUpdateListener,
                (reportId, selectedStatus) -> selectedStatusByReportId.put(reportId, selectedStatus)
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener == null) {
                return;
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            listener.onItemClicked(items.get(currentPosition), currentPosition);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener == null) {
                return;
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (!showDeleteAction || currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            listener.onDeleteClicked(items.get(currentPosition), currentPosition);
        });

        holder.acceptTaskButton.setOnClickListener(v -> {
            if (taskActionListener == null) {
                return;
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            taskActionListener.onAcceptTask(items.get(currentPosition), currentPosition);
        });

        holder.rejectTaskButton.setOnClickListener(v -> {
            if (taskActionListener == null) {
                return;
            }
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            taskActionListener.onRejectTask(items.get(currentPosition), currentPosition);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EmergencyReportViewHolder extends RecyclerView.ViewHolder {

        interface StatusSelectionStore {
            void onStatusSelected(@NonNull String reportId, @NonNull String statusCode);
        }

        private static final String STATUS_DANG_TREN_DUONG_TOI = "DANG_TREN_DUONG_TOI";
        private static final String STATUS_DANG_XU_LY = "DANG_XU_LY";
        private static final String STATUS_HOAN_THANH = "HOAN_THANH";
        private static final String STATUS_HUY = "HUY";

        private final TextView locationTextView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView statusTextView;
        private final ImageView iconImageView;
        private final ImageButton deleteButton;
        private final LinearLayout taskActionLayout;
        private final Button acceptTaskButton;
        private final Button rejectTaskButton;
        private final LinearLayout taskStatusUpdateLayout;
        private final Spinner taskStatusSpinner;
        private final Button taskUpdateStatusButton;

        EmergencyReportViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.tvReportItemLocation);
            titleTextView = itemView.findViewById(R.id.tvReportItemTitle);
            descriptionTextView = itemView.findViewById(R.id.tvReportItemDescription);
            statusTextView = itemView.findViewById(R.id.tvReportItemStatus);
            iconImageView = itemView.findViewById(R.id.ivReportItemTypeIcon);
            deleteButton = itemView.findViewById(R.id.btnDeleteReport);
            taskActionLayout = itemView.findViewById(R.id.layoutTaskActions);
            acceptTaskButton = itemView.findViewById(R.id.btnTaskAccept);
            rejectTaskButton = itemView.findViewById(R.id.btnTaskReject);
            taskStatusUpdateLayout = itemView.findViewById(R.id.layoutTaskStatusUpdate);
            taskStatusSpinner = itemView.findViewById(R.id.spinnerTaskStatus);
            taskUpdateStatusButton = itemView.findViewById(R.id.btnTaskUpdateStatus);
        }

        void bind(
                @NonNull EmergencyReportItem item,
                boolean canDelete,
                boolean canShowTaskActions,
                boolean canShowTaskStatusUpdater,
                @NonNull ReportBitmapLoader bitmapLoader
        ) {
            locationTextView.setText(item.getLocation());
            titleTextView.setText(item.getTitle());
            descriptionTextView.setText(item.getDescription());
            bindIncidentIcon(item, bitmapLoader);
            deleteButton.setVisibility(canDelete ? View.VISIBLE : View.GONE);

            String statusLabel = item.getStatusLabel();
            if (statusLabel == null || statusLabel.trim().isEmpty()) {
                statusLabel = item.getStatus();
            }
            if (statusLabel == null || statusLabel.trim().isEmpty()) {
                statusTextView.setVisibility(View.GONE);
            } else {
                statusTextView.setVisibility(View.VISIBLE);
                statusTextView.setText(statusLabel);
            }

            boolean isChoDieuPhoi = item.getStatus() != null
                    && "CHO_DIEU_PHOI".equalsIgnoreCase(item.getStatus().trim());
            taskActionLayout.setVisibility(
                    canShowTaskActions && isChoDieuPhoi ? View.VISIBLE : View.GONE
            );
            taskStatusUpdateLayout.setVisibility(
                    canShowTaskStatusUpdater && !isChoDieuPhoi ? View.VISIBLE : View.GONE
            );
        }

        private void bindIncidentIcon(
                @NonNull EmergencyReportItem item,
                @NonNull ReportBitmapLoader bitmapLoader
        ) {
            String targetUrl = bitmapLoader.normalizeUrl(item.getIconUrl());
            if (targetUrl == null || !bitmapLoader.isHttpUrl(targetUrl)) {
                setLocalFallbackIcon(item.getIconResId(), false);
                return;
            }

            iconImageView.setTag(targetUrl);
            setLocalFallbackIcon(item.getIconResId(), true);
            bitmapLoader.load(targetUrl, (loadedUrl, bitmap) -> {
                Object boundTag = iconImageView.getTag();
                if (!(boundTag instanceof String)) {
                    return;
                }
                String expectedUrl = (String) boundTag;
                if (loadedUrl == null || !expectedUrl.equals(loadedUrl)) {
                    return;
                }
                if (bitmap == null) {
                    setLocalFallbackIcon(item.getIconResId(), true);
                    return;
                }
                iconImageView.setImageBitmap(bitmap);
                iconImageView.setImageTintList(null);
                iconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int loadedPadding = dpToPx(2);
                iconImageView.setPadding(loadedPadding, loadedPadding, loadedPadding, loadedPadding);
            });
        }

        private void setLocalFallbackIcon(int iconResId, boolean preserveTag) {
            if (!preserveTag) {
                iconImageView.setTag(null);
            }
            iconImageView.setImageResource(iconResId);
            iconImageView.setImageTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.white))
            );
            iconImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int padding = dpToPx(14);
            iconImageView.setPadding(padding, padding, padding, padding);
        }

        private int dpToPx(int dp) {
            return Math.round(dp * itemView.getResources().getDisplayMetrics().density);
        }

        void bindStatusUpdater(
                @NonNull EmergencyReportItem item,
                @Nullable String selectedStatusCode,
                @Nullable StatusUpdateListener statusUpdateListener,
                @NonNull StatusSelectionStore selectionStore
        ) {
            if (taskStatusUpdateLayout.getVisibility() != View.VISIBLE) {
                return;
            }

            List<StatusOption> options = buildStatusOptions();
            List<String> labels = new ArrayList<>(options.size());
            for (StatusOption option : options) {
                labels.add(option.label);
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    itemView.getContext(),
                    android.R.layout.simple_spinner_item,
                    labels
            );
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            taskStatusSpinner.setAdapter(spinnerAdapter);

            String normalizedSelected = normalizeStatus(selectedStatusCode);
            if (normalizedSelected == null) {
                normalizedSelected = normalizeStatus(item.getStatus());
            }
            if (!containsStatusCode(options, normalizedSelected)) {
                normalizedSelected = STATUS_DANG_TREN_DUONG_TOI;
            }

            int selectedIndex = findStatusIndex(options, normalizedSelected);
            taskStatusSpinner.setSelection(selectedIndex, false);
            selectionStore.onStatusSelected(item.getId(), options.get(selectedIndex).code);

            taskStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position < 0 || position >= options.size()) {
                        return;
                    }
                    selectionStore.onStatusSelected(item.getId(), options.get(position).code);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // No-op.
                }
            });

            taskUpdateStatusButton.setOnClickListener(v -> {
                if (statusUpdateListener == null) {
                    return;
                }
                int currentPosition = getBindingAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                int spinnerPosition = taskStatusSpinner.getSelectedItemPosition();
                if (spinnerPosition < 0 || spinnerPosition >= options.size()) {
                    return;
                }
                statusUpdateListener.onUpdateTaskStatus(
                        item,
                        currentPosition,
                        options.get(spinnerPosition).code
                );
            });
        }

        @NonNull
        private List<StatusOption> buildStatusOptions() {
            List<StatusOption> options = new ArrayList<>();
            options.add(new StatusOption(
                    STATUS_DANG_TREN_DUONG_TOI,
                    itemView.getContext().getString(R.string.captain_task_status_en_route)
            ));
            options.add(new StatusOption(
                    STATUS_DANG_XU_LY,
                    itemView.getContext().getString(R.string.captain_task_status_processing)
            ));
            options.add(new StatusOption(
                    STATUS_HOAN_THANH,
                    itemView.getContext().getString(R.string.captain_task_status_completed)
            ));
            options.add(new StatusOption(
                    STATUS_HUY,
                    itemView.getContext().getString(R.string.captain_task_status_cancelled)
            ));
            return options;
        }

        private boolean containsStatusCode(@NonNull List<StatusOption> options, @Nullable String targetCode) {
            if (targetCode == null) {
                return false;
            }
            for (StatusOption option : options) {
                if (option.code.equals(targetCode)) {
                    return true;
                }
            }
            return false;
        }

        private int findStatusIndex(@NonNull List<StatusOption> options, @NonNull String targetCode) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).code.equals(targetCode)) {
                    return i;
                }
            }
            return 0;
        }

        @Nullable
        private String normalizeStatus(@Nullable String rawStatus) {
            if (rawStatus == null) {
                return null;
            }
            String trimmed = rawStatus.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return trimmed.toUpperCase(Locale.ROOT);
        }

        private static class StatusOption {
            @NonNull
            private final String code;
            @NonNull
            private final String label;

            private StatusOption(@NonNull String code, @NonNull String label) {
                this.code = code;
                this.label = label;
            }
        }
    }
}
