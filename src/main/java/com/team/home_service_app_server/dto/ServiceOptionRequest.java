package com.team.home_service_app_server.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ServiceOptionRequest(
		String name,
		BigDecimal price,
		String unit,
		@JsonProperty("display_order") @JsonAlias({ "sort_order", "sortOrder", "sequence" }) Integer displayOrder
) {
}
