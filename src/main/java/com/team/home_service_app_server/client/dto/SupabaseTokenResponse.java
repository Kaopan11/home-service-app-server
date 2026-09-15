package com.team.home_service_app_server.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SupabaseTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("expires_in") long expiresIn,
		@JsonProperty("token_type") String tokenType
) {
}
