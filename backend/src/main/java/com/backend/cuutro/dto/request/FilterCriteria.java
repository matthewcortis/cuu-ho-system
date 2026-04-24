package com.backend.cuutro.dto.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.backend.cuutro.constant.enums.FilterLogicType;
import com.backend.cuutro.constant.enums.FilterOperation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilterCriteria implements Serializable {

	@JsonProperty("fieldName")
	@JsonAlias("field")
	String fieldName;
	FilterOperation operation;
	Object value;
	@JsonProperty("logicType")
	@JsonAlias("logic")
	@Builder.Default
	FilterLogicType logicType = FilterLogicType.AND;
}
