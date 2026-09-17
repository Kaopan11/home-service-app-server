package com.team.home_service_app_server.dto;

public record ServiceListItemDto(
		Long id,
		String name,
		String categoryName,
		Integer sortOrder
) {
}
