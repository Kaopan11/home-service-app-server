package com.team.home_service_app_server.dto.technician;

public record TechnicianJobDto(
		Long id,
		String serviceName,
		String customerName,
		String address,
		String status
) {
}
