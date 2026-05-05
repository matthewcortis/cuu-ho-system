package com.example.cuutro.features.sos.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuutro.R;
import com.example.cuutro.features.sos.model.EmergencyReportItem;

import java.util.ArrayList;
import java.util.List;

public class EmergencyReportAdapter extends RecyclerView.Adapter<EmergencyReportAdapter.EmergencyReportViewHolder> {

    public interface Listener {
        void onDeleteClicked(@NonNull EmergencyReportItem item, int position);

        void onItemClicked(@NonNull EmergencyReportItem item, int position);
    }

    public interface TaskActionListener {
        void onAcceptTask(@NonNull EmergencyReportItem item, int position);

        void onRejectTask(@NonNull EmergencyReportItem item, int position);
    }

    private final List<EmergencyReportItem> items = new ArrayList<>();
    @Nullable
    private final Listener listener;
    @Nullable
    private TaskActionListener taskActionListener;
    private boolean showDeleteAction = true;
    private boolean showTaskActions = false;

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
        holder.bind(item, showDeleteAction, showTaskActions);

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

        private final TextView locationTextView;
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView statusTextView;
        private final ImageView iconImageView;
        private final ImageButton deleteButton;
        private final LinearLayout taskActionLayout;
        private final Button acceptTaskButton;
        private final Button rejectTaskButton;

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
        }

        void bind(@NonNull EmergencyReportItem item, boolean canDelete, boolean canShowTaskActions) {
            locationTextView.setText(item.getLocation());
            titleTextView.setText(item.getTitle());
            descriptionTextView.setText(item.getDescription());
            iconImageView.setImageResource(item.getIconResId());
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
        }
    }
}
