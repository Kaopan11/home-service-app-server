package com.team.home_service_app_server.dto.technician;

import java.math.BigDecimal;
import java.time.Instant;

public record TechnicianJobDetailDto(
		Long id,
		String orderCode,
		Long serviceId,
		String serviceName,
		Long categoryId,
		String categoryName,
		String itemsDescription,
		Instant scheduledAt,
		String address,
		Double latitude,
		Double longitude,
		BigDecimal totalPrice,
		String customerName,
		String customerPhone,
		Integer rating,
		String reviewComment,
		String status,
		Instant createdAt
) {
}
