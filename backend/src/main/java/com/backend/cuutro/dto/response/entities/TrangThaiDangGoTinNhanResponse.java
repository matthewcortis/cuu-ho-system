package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class TrangThaiDangGoTinNhanResponse implements Serializable {

	Long phieuId;
	UUID nguoiDungId;
	Boolean dangGo;
	Instant updatedAt;
}

