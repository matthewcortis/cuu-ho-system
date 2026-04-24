package com.backend.cuutro.dto.request;

import java.util.ArrayList;
import java.util.List;

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
public class LoaiSuCoFilterRequest {

	@Builder.Default
	List<String> ten = new ArrayList<>();

	@Builder.Default
	List<Boolean> trangThai = new ArrayList<>();
}
