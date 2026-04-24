package com.backend.cuutro.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
public class NhomVatPhamUpsertRequest {

	@NotBlank(message = "ten is required")
	String ten;

	String moTa;

	@Positive(message = "loaiSuCoId must be greater than 0")
	Long loaiSuCoId;

	List<@Positive(message = "loaiSuCoIds must contain values greater than 0") Long> loaiSuCoIds;
}
