package com.team.home_service_app_server.dto;

import java.math.BigDecimal;

public record ServiceListItemDto(
		Long id,
		String name,
		String categoryName,
		Integer sortOrder,
		String image,
		BigDecimal priceMin
) {
}
