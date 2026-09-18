package com.team.home_service_app_server.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PromoDiscountType {
	FIXED,
	PERCENT;

	@JsonValue
	public String toJson() {
		return name().toLowerCase();
	}

	@JsonCreator
	public static PromoDiscountType fromJson(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return PromoDiscountType.valueOf(value.trim().toUpperCase());
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}
