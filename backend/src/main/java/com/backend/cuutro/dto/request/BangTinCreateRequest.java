package com.backend.cuutro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class BangTinCreateRequest {

	@NotBlank(message = "tieuDe is required")
	String tieuDe;

	@NotBlank(message = "noiDung is required")
	String noiDung;

	Long tepTinId;

	Long viTriId;

	@Valid
	ViTriInputRequest viTri;
}
