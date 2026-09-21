package com.team.home_service_app_server.dto.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record CategoryDto(
		@JsonProperty("category_id") Integer categoryId,
		String name,
		@JsonProperty("is_active") boolean active,
		@JsonProperty("created_at") @JsonFormat(
				shape = JsonFormat.Shape.STRING,
				pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				timezone = "UTC") Instant createdAt,
		@JsonProperty("updated_at") @JsonFormat(
				shape = JsonFormat.Shape.STRING,
				pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				timezone = "UTC") Instant updatedAt
) {
}
