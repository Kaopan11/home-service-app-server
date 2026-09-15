package com.team.home_service_app_server.dto;

public record SessionDto(
		String accessToken,
		String refreshToken,
		long expiresAt,
		String tokenType
) {
}
