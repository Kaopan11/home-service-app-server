package com.team.home_service_app_server.dto;

import java.math.BigDecimal;

public record ServiceOptionDto(
		Long id,
		String name,
		String unit,
		BigDecimal price
) {
}
