package com.example.cuutro.features.report.data.remote.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LoaiSuCoFilterRequestDto {

    @SerializedName("filters")
    private final List<FilterCriteriaDto> filters;

    @SerializedName("sorts")
    private final List<SortCriteriaDto> sorts;

    @SerializedName("page")
    private final int page;

    @SerializedName("size")
    private final int size;

    public LoaiSuCoFilterRequestDto(
            List<FilterCriteriaDto> filters,
            List<SortCriteriaDto> sorts,
            int page,
            int size
    ) {
        this.filters = filters == null ? new ArrayList<>() : filters;
        this.sorts = sorts == null ? new ArrayList<>() : sorts;
        this.page = page;
        this.size = size;
    }

    public static class FilterCriteriaDto {

        @SerializedName("fieldName")
        private final String fieldName;

        @SerializedName("operation")
        private final String operation;

        @SerializedName("value")
        private final Object value;

        @SerializedName("logicType")
        private final String logicType;

        public FilterCriteriaDto(String fieldName, String operation, Object value, String logicType) {
            this.fieldName = fieldName;
            this.operation = operation;
            this.value = value;
            this.logicType = logicType;
        }
    }

    public static class SortCriteriaDto {

        @SerializedName("fieldName")
        private final String fieldName;

        @SerializedName("direction")
        private final String direction;

        public SortCriteriaDto(String fieldName, String direction) {
            this.fieldName = fieldName;
            this.direction = direction;
        }
    }
}
