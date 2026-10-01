package com.team.home_service_app_server.dto;

import jakarta.validation.constraints.NotBlank;

public record FacebookLoginRequest(
		@NotBlank(message = "ไม่พบ access token จาก Facebook") String accessToken,
		String refreshToken,
		Long expiresIn
) {
}
