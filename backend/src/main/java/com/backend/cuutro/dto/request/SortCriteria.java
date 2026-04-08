package com.backend.cuutro.dto.request;

import java.io.Serializable;

import com.backend.cuutro.constant.enums.SortDirection;

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
public class SortCriteria implements Serializable {

	String field;
	@Builder.Default
	SortDirection direction = SortDirection.ASC;
}

