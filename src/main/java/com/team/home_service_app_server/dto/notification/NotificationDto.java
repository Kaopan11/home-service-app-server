package com.team.home_service_app_server.dto.notification;

import java.time.Instant;

public record NotificationDto(
		Long id,
		String type,
		String title,
		String body,
		Long jobId,
		boolean read,
		Instant createdAt
) {
}
