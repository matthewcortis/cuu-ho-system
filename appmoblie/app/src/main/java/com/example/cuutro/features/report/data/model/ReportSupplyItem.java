package com.example.cuutro.features.report.data.model;

import androidx.annotation.Nullable;

public class ReportSupplyItem {

    @Nullable
    private final Long id;
    @Nullable
    private final Long groupId;
    private final String groupName;
    private final String name;
    @Nullable
    private final String imageUrl;
    @Nullable
    private final Integer quantity;
    @Nullable
    private final String unitName;

    public ReportSupplyItem(
            @Nullable Long id,
            @Nullable Long groupId,
            String groupName,
            String name,
            @Nullable String imageUrl,
            @Nullable Integer quantity,
            @Nullable String unitName
    ) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.name = name;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.unitName = unitName;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    @Nullable
    public Long getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    public Integer getQuantity() {
        return quantity;
    }

    @Nullable
    public String getUnitName() {
        return unitName;
    }
}
