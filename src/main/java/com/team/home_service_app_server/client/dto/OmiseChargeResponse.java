package com.team.home_service_app_server.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OmiseChargeResponse(
		String id,
		String status,
		Long amount,
		String currency,
		boolean paid,
		@JsonProperty("failure_code") String failureCode,
		@JsonProperty("failure_message") String failureMessage
) {
}
