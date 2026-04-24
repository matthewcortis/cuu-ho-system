package com.example.cuutro.features.report.ui.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.cuutro.R;
import com.example.cuutro.features.report.data.model.ReportEmergencyType;

import java.util.ArrayList;
import java.util.List;

public class EmergencyTypeSectionController {

    public interface Listener {
        void onEmergencyTypeSelected(@NonNull ReportEmergencyType emergencyType, int position);
    }

    private final Context context;
    private final GridLayout gridLayout;
    private final LayoutInflater inflater;
    private final ReportBitmapLoader bitmapLoader;
    private final Listener listener;

    private final List<ReportEmergencyType> emergencyTypes = new ArrayList<>();
    private final List<View> emergencyItemViews = new ArrayList<>();

    private final int selectedTextColor;
    private final int unselectedTextColor;
    private final int selectedIconTint;
    private final int unselectedIconTint;

    private int selectedIndex = -1;
    private int renderGeneration = 0;

    public EmergencyTypeSectionController(
            @NonNull Context context,
            @NonNull GridLayout gridLayout,
            @NonNull ReportBitmapLoader bitmapLoader,
            @NonNull Listener listener
    ) {
        this.context = context;
        this.gridLayout = gridLayout;
        this.inflater = LayoutInflater.from(context);
        this.bitmapLoader = bitmapLoader;
        this.listener = listener;

        selectedTextColor = ContextCompat.getColor(context, R.color.report_text_primary);
        unselectedTextColor = ContextCompat.getColor(context, R.color.report_label_inactive);
        selectedIconTint = ContextCompat.getColor(context, R.color.white);
        unselectedIconTint = ContextCompat.getColor(context, R.color.report_icon_inactive);
    }

    public void setItems(@Nullable List<ReportEmergencyType> items) {
        emergencyTypes.clear();
        if (items != null) {
            emergencyTypes.addAll(items);
        }

        if (emergencyTypes.isEmpty()) {
            selectedIndex = -1;
            renderGrid();
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= emergencyTypes.size()) {
            selectedIndex = 0;
        }

        renderGrid();
        selectInternal(selectedIndex, true, true);
    }

    public void clear() {
        setItems(null);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Nullable
    public ReportEmergencyType getSelectedItem() {
        if (selectedIndex < 0 || selectedIndex >= emergencyTypes.size()) {
            return null;
        }
        return emergencyTypes.get(selectedIndex);
    }

    public void release() {
        renderGeneration++;
    }

    private void renderGrid() {
        int generationToken = ++renderGeneration;
        gridLayout.removeAllViews();
        emergencyItemViews.clear();

        for (int i = 0; i < emergencyTypes.size(); i++) {
            ReportEmergencyType emergencyType = emergencyTypes.get(i);
            View itemView = inflater.inflate(R.layout.item_emergency_type, gridLayout, false);

            EmergencyTypeViewHolder holder = new EmergencyTypeViewHolder(itemView);
            holder.nameView.setText(emergencyType.getLabel());
            bindEmergencyIcon(holder, emergencyType, generationToken);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            int horizontalMargin = dpToPx(2);
            int verticalMargin = dpToPx(8);
            params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
            itemView.setLayoutParams(params);

            int position = i;
            itemView.setOnClickListener(v -> selectInternal(position, true, false));

            itemView.setTag(holder);
            emergencyItemViews.add(itemView);
            gridLayout.addView(itemView);
        }

        applySelectionUi();
    }

    private void selectInternal(int position, boolean notify, boolean forceNotify) {
        if (position < 0 || position >= emergencyTypes.size()) {
            return;
        }

        boolean selectionChanged = selectedIndex != position;
        selectedIndex = position;
        applySelectionUi();

        if (notify && listener != null && (selectionChanged || forceNotify)) {
            listener.onEmergencyTypeSelected(emergencyTypes.get(position), position);
        }
    }

    private void applySelectionUi() {
        for (int i = 0; i < emergencyItemViews.size(); i++) {
            View itemView = emergencyItemViews.get(i);
            Object tag = itemView.getTag();
            if (!(tag instanceof EmergencyTypeViewHolder)) {
                continue;
            }

            EmergencyTypeViewHolder holder = (EmergencyTypeViewHolder) tag;
            boolean isSelected = i == selectedIndex;

            holder.iconContainer.setBackgroundResource(
                    isSelected
                            ? R.drawable.bg_report_emergency_icon_selected
                            : R.drawable.bg_report_emergency_icon_unselected
            );
            if (holder.usingRemoteIcon) {
                holder.iconView.setImageTintList(null);
            } else {
                holder.iconView.setImageTintList(
                        ColorStateList.valueOf(isSelected ? selectedIconTint : unselectedIconTint)
                );
            }
            holder.nameView.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);
        }
    }

    private void bindEmergencyIcon(
            @NonNull EmergencyTypeViewHolder holder,
            @NonNull ReportEmergencyType emergencyType,
            int generationToken
    ) {
        holder.usingRemoteIcon = false;
        holder.boundIconUrl = bitmapLoader.normalizeUrl(emergencyType.getIconUrl());
        holder.iconView.setImageResource(emergencyType.getIconResId());
        holder.iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.iconView.setImageTintList(ColorStateList.valueOf(unselectedIconTint));

        String targetUrl = holder.boundIconUrl;
        if (!bitmapLoader.isHttpUrl(targetUrl)) {
            return;
        }

        bitmapLoader.load(targetUrl, (normalizedUrl, bitmap) -> {
            if (bitmap == null) {
                return;
            }
            if (generationToken != renderGeneration) {
                return;
            }
            if (holder.boundIconUrl == null || !holder.boundIconUrl.equals(normalizedUrl)) {
                return;
            }
            holder.usingRemoteIcon = true;
            holder.iconView.setImageBitmap(bitmap);
            holder.iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            holder.iconView.setImageTintList(null);
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    private static class EmergencyTypeViewHolder {
        final FrameLayout iconContainer;
        final ImageView iconView;
        final TextView nameView;
        boolean usingRemoteIcon;
        String boundIconUrl;

        EmergencyTypeViewHolder(@NonNull View itemView) {
            iconContainer = itemView.findViewById(R.id.flEmergencyIconContainer);
            iconView = itemView.findViewById(R.id.ivEmergencyIcon);
            nameView = itemView.findViewById(R.id.tvEmergencyName);
        }
    }
}
