package com.backend.cuutro.dto.request;

import jakarta.validation.Valid;
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
public class GuiTinNhanPhieuRequest {

	String noiDung;

	Long tepTinId;

	Long viTriId;

	@Valid
	ViTriInputRequest viTri;
}
