package com.backend.cuutro.dto.response.entities;

import java.io.Serializable;
import java.util.UUID;

import com.backend.cuutro.constant.enums.LoaiNguoiGui;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class NguoiGuiDto implements Serializable {

	LoaiNguoiGui type;
	UUID userId;
	String ten;
	String sdt;
}
