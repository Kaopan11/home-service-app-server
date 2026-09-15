package com.team.home_service_app_server.dto;

import java.time.Instant;

public record AdminServiceDto(
		Long id,
		Integer sortOrder,
		String name,
		String categoryName,
		String categoryTone,
		Instant createdAt,
		Instant updatedAt
) {
}
