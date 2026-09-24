package com.team.home_service_app_server.dto.technician;

import java.time.Instant;

public record TechnicianJobDto(
		Long id,
		String serviceName,
		String customerName,
		String address,
		Double latitude,
		Double longitude,
		String status,
		Instant createdAt
) {
}
