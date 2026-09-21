package com.team.home_service_app_server.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PromoDiscountTypeConverter implements AttributeConverter<PromoDiscountType, String> {

	@Override
	public String convertToDatabaseColumn(PromoDiscountType attribute) {
		return attribute == null ? null : attribute.toJson();
	}

	@Override
	public PromoDiscountType convertToEntityAttribute(String dbData) {
		return PromoDiscountType.fromJson(dbData);
	}
}
