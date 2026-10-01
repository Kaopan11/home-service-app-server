package com.team.home_service_app_server.dto.technician;

import java.math.BigDecimal;
import java.time.Instant;

public record TechnicianJobDto(
		Long id,
		String serviceName,
		String customerName,
		String address,
		Double latitude,
		Double longitude,
		String status,
		Instant createdAt,
		String orderCode,
		String itemsDescription,
		Instant scheduledAt,
		BigDecimal totalPrice
) {
}
