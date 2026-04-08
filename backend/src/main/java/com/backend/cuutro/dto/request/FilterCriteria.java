package com.backend.cuutro.dto.request;

import java.io.Serializable;

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

	String field;
	FilterOperation operation;
	Object value;
	@Builder.Default
	FilterLogicType logic = FilterLogicType.AND;
}

