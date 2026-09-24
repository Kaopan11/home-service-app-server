package com.team.home_service_app_server.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServiceOptionDto(
		Long id,
		String name,
		String unit,
		BigDecimal price,
		@JsonProperty("display_order") Integer displayOrder
) {
}
