package com.example.cuutro.features.report.ui.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.cuutro.R;
import com.example.cuutro.features.report.data.model.ReportSupplyItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SupplySectionController {

    private static final String DEFAULT_GROUP_NAME = "Nhóm vật phẩm";

    private final Context context;
    private final LayoutInflater inflater;
    private final LinearLayout listContainer;
    private final TextView stateTextView;
    private final ReportBitmapLoader bitmapLoader;

    private final List<ReportSupplyItem> supplyItems = new ArrayList<>();
    private final Map<String, Boolean> groupExpandedState = new LinkedHashMap<>();
    private final Map<String, Boolean> checkedState = new LinkedHashMap<>();
    private final Map<String, Integer> selectedQuantity = new LinkedHashMap<>();

    private final int placeholderTintColor;
    private int renderGeneration = 0;

    public SupplySectionController(
            @NonNull Context context,
            @NonNull LinearLayout listContainer,
            @NonNull TextView stateTextView,
            @NonNull ReportBitmapLoader bitmapLoader
    ) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listContainer = listContainer;
        this.stateTextView = stateTextView;
        this.bitmapLoader = bitmapLoader;
        this.placeholderTintColor = ContextCompat.getColor(context, R.color.report_icon_inactive);
    }

    public void setItems(@Nullable List<ReportSupplyItem> items) {
        supplyItems.clear();
        if (items != null) {
            supplyItems.addAll(items);
        }
        clearSelectionState();
        render();
    }

    public void clear() {
        supplyItems.clear();
        clearSelectionState();
        render();
    }

    public boolean isEmpty() {
        return supplyItems.isEmpty();
    }

    public void showState(@NonNull String message) {
        stateTextView.setText(message);
        stateTextView.setVisibility(View.VISIBLE);
    }

    public void hideState() {
        stateTextView.setVisibility(View.GONE);
    }

    @NonNull
    public List<SelectedSupply> getSelectedSupplies() {
        List<SelectedSupply> selected = new ArrayList<>();
        if (supplyItems.isEmpty()) {
            return selected;
        }

        for (ReportSupplyItem item : supplyItems) {
            if (item == null) {
                continue;
            }
            String key = buildSupplyKey(item);
            if (!Boolean.TRUE.equals(checkedState.get(key))) {
                continue;
            }
            int quantity = Math.max(1, selectedQuantity.getOrDefault(key, 1));
            if (item.getId() == null || item.getId() <= 0) {
                continue;
            }
            selected.add(new SelectedSupply(item, quantity));
        }
        return selected;
    }

    public void release() {
        renderGeneration++;
    }

    private void render() {
        int generationToken = ++renderGeneration;
        listContainer.removeAllViews();

        if (supplyItems.isEmpty()) {
            return;
        }

        List<SupplyGroup> groups = buildSupplyGroups(supplyItems);
        for (SupplyGroup group : groups) {
            View headerView = inflater.inflate(R.layout.item_report_supply_group, listContainer, false);
            TextView groupNameView = headerView.findViewById(R.id.tvSupplyGroupName);
            ImageView toggleView = headerView.findViewById(R.id.ivSupplyGroupToggle);

            boolean expanded = Boolean.TRUE.equals(groupExpandedState.get(group.key));
            groupNameView.setText(group.name);
            toggleView.setRotation(expanded ? 90f : 0f);
            toggleView.setContentDescription(context.getString(
                    expanded ? R.string.report_supply_group_collapse : R.string.report_supply_group_expand
            ));

            headerView.setOnClickListener(v -> {
                boolean currentExpanded = Boolean.TRUE.equals(groupExpandedState.get(group.key));
                groupExpandedState.put(group.key, !currentExpanded);
                render();
            });
            listContainer.addView(headerView);

            if (!expanded) {
                continue;
            }

            for (ReportSupplyItem item : group.items) {
                View itemView = inflater.inflate(R.layout.item_report_supply_selectable, listContainer, false);
                bindSupplyItemView(itemView, item, generationToken);
                listContainer.addView(itemView);
            }
        }
    }

    private void bindSupplyItemView(@NonNull View itemView, @NonNull ReportSupplyItem item, int generationToken) {
        CheckBox selectCheckBox = itemView.findViewById(R.id.cbSupplySelect);
        ImageView imageView = itemView.findViewById(R.id.ivSupplyImage);
        TextView nameView = itemView.findViewById(R.id.tvSupplyName);
        LinearLayout quantityContainer = itemView.findViewById(R.id.layoutSupplyQuantityControls);
        TextView quantityValueView = itemView.findViewById(R.id.tvSupplySelectedQuantity);
        TextView minusButton = itemView.findViewById(R.id.btnSupplyMinus);
        TextView plusButton = itemView.findViewById(R.id.btnSupplyPlus);

        String supplyKey = buildSupplyKey(item);
        boolean checked = Boolean.TRUE.equals(checkedState.get(supplyKey));
        int quantity = Math.max(1, selectedQuantity.getOrDefault(supplyKey, 1));

        nameView.setText(item.getName());
        bindSupplyImage(imageView, item, generationToken);

        selectCheckBox.setOnCheckedChangeListener(null);
        selectCheckBox.setChecked(checked);
        selectCheckBox.setContentDescription(context.getString(R.string.report_supply_select_item, item.getName()));
        selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedState.put(supplyKey, isChecked);
            if (isChecked) {
                selectedQuantity.put(supplyKey, Math.max(1, selectedQuantity.getOrDefault(supplyKey, 1)));
            } else {
                selectedQuantity.remove(supplyKey);
            }
            render();
        });

        quantityContainer.setVisibility(checked ? View.VISIBLE : View.GONE);
        quantityValueView.setText(String.valueOf(quantity));

        minusButton.setOnClickListener(v -> {
            int current = Math.max(1, selectedQuantity.getOrDefault(supplyKey, 1));
            selectedQuantity.put(supplyKey, Math.max(1, current - 1));
            render();
        });

        plusButton.setOnClickListener(v -> {
            int current = Math.max(1, selectedQuantity.getOrDefault(supplyKey, 1));
            selectedQuantity.put(supplyKey, current + 1);
            render();
        });
    }

    private void bindSupplyImage(@NonNull ImageView imageView, @NonNull ReportSupplyItem item, int generationToken) {
        imageView.setImageResource(R.drawable.ic_emergency_other);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageTintList(ColorStateList.valueOf(placeholderTintColor));

        String imageUrl = bitmapLoader.normalizeUrl(item.getImageUrl());
        imageView.setTag(imageUrl);
        if (!bitmapLoader.isHttpUrl(imageUrl)) {
            return;
        }

        bitmapLoader.load(imageUrl, (normalizedUrl, bitmap) -> {
            if (bitmap == null) {
                return;
            }
            if (generationToken != renderGeneration) {
                return;
            }
            Object boundTag = imageView.getTag();
            if (!(boundTag instanceof String)) {
                return;
            }
            if (!((String) boundTag).equals(normalizedUrl)) {
                return;
            }
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageTintList(null);
        });
    }

    @NonNull
    private List<SupplyGroup> buildSupplyGroups(@NonNull List<ReportSupplyItem> items) {
        Map<String, SupplyGroup> grouped = new LinkedHashMap<>();
        for (ReportSupplyItem item : items) {
            String key = buildGroupKey(item);
            SupplyGroup group = grouped.get(key);
            if (group == null) {
                group = new SupplyGroup(key, item.getGroupName());
                grouped.put(key, group);
                groupExpandedState.putIfAbsent(key, true);
            }
            group.items.add(item);
        }
        return new ArrayList<>(grouped.values());
    }

    private void clearSelectionState() {
        groupExpandedState.clear();
        checkedState.clear();
        selectedQuantity.clear();
    }

    @NonNull
    private String buildGroupKey(@NonNull ReportSupplyItem item) {
        if (item.getGroupId() != null && item.getGroupId() > 0) {
            return "group-id-" + item.getGroupId();
        }
        return "group-name-" + normalizeForKey(item.getGroupName());
    }

    @NonNull
    private String buildSupplyKey(@NonNull ReportSupplyItem item) {
        if (item.getId() != null && item.getId() > 0) {
            return "supply-id-" + item.getId();
        }
        return buildGroupKey(item) + "-name-" + normalizeForKey(item.getName());
    }

    @NonNull
    private String normalizeForKey(@Nullable String raw) {
        if (raw == null) {
            return "unknown";
        }
        return raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    private static final class SupplyGroup {
        final String key;
        final String name;
        final List<ReportSupplyItem> items = new ArrayList<>();

        private SupplyGroup(@NonNull String key, @Nullable String name) {
            this.key = key;
            this.name = (name == null || name.trim().isEmpty()) ? DEFAULT_GROUP_NAME : name.trim();
        }
    }

    public static final class SelectedSupply {
        private final ReportSupplyItem item;
        private final int quantity;

        private SelectedSupply(@NonNull ReportSupplyItem item, int quantity) {
            this.item = item;
            this.quantity = Math.max(1, quantity);
        }

        @NonNull
        public ReportSupplyItem getItem() {
            return item;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
