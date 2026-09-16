package com.team.home_service_app_server.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PatchCategoryRequest(
		String name,
		@JsonProperty("is_active") Boolean active
) {
}
