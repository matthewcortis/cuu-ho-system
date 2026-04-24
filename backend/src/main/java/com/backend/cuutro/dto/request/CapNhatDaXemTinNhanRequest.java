package com.backend.cuutro.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class CapNhatDaXemTinNhanRequest {

	@NotNull(message = "lastSeenMessageId is required")
	@Positive(message = "lastSeenMessageId must be greater than 0")
	Long lastSeenMessageId;
}

