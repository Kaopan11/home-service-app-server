package com.team.home_service_app_server.dto.technician;

import java.math.BigDecimal;
import java.time.Instant;

public record TechnicianJobItemDto(
		Long id,
		String orderCode,
		Long serviceId,
		String serviceName,
		Long categoryId,
		String categoryName,
		Instant scheduledAt,
		BigDecimal totalPrice,
		String status,
		String address,
		String customerName
) {
}
