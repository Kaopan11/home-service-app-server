package com.team.home_service_app_server.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminServiceDto(
		Long id,
		Integer sortOrder,
		String name,
		@JsonProperty("categoryId") Integer categoryId,
		String categoryName,
		String categoryTone,
		@JsonProperty("imageUrl") String imageUrl,
		List<ServiceOptionDto> options,
		Instant createdAt,
		Instant updatedAt
) {
}
