package com.team.home_service_app_server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateServiceRequest(
		String name,
		@JsonProperty("category_id") @JsonAlias("categoryId") Integer categoryId,
		@JsonProperty("image_url") @JsonAlias({ "image", "imageUrl" }) String imageUrl,
		@JsonProperty("options") @JsonAlias({ "sub_items", "line_items", "variants", "items" })
		List<ServiceOptionRequest> options,
		@JsonProperty("display_order") @JsonAlias({ "sort_order", "sortOrder", "sequence" }) Integer displayOrder
) {
}
