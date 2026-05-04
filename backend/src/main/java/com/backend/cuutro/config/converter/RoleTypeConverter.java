package com.backend.cuutro.config.converter;

import org.springframework.util.StringUtils;

import com.backend.cuutro.constant.enums.RoleType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleTypeConverter implements AttributeConverter<RoleType, String> {

	@Override
	public String convertToDatabaseColumn(RoleType attribute) {
		if (attribute == null) {
			return null;
		}
		if (attribute == RoleType.NGUOI_DAN) {
			return "USER";
		}
		if (attribute == RoleType.TRUONG_NHOM_TNV) {
			return RoleType.TRUONG_NHOM_TNV.name();
		}
		return attribute.name();
	}

	@Override
	public RoleType convertToEntityAttribute(String dbData) {
		if (!StringUtils.hasText(dbData)) {
			return null;
		}

		String normalized = dbData.trim().toUpperCase();
		if ("USER".equals(normalized)) {
			return RoleType.NGUOI_DAN;
		}
		if ("VOLUNTEER".equals(normalized) || RoleType.TRUONG_NHOM_TNV.name().equals(normalized)) {
			return RoleType.TRUONG_NHOM_TNV;
		}
		return RoleType.valueOf(normalized);
	}
}
