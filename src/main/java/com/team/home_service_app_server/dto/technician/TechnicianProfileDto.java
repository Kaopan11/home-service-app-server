package com.team.home_service_app_server.dto.technician;

import java.util.List;

public record TechnicianProfileDto(
		Long id,
		String email,
		String fullName,
		String displayName,
		String firstName,
		String lastName,
		String phone,
		String address,
		String avatarUrl,
		String role,
		Double latitude,
		Double longitude,
		boolean available,
		List<Long> acceptedServiceIds,
		List<TechnicianServiceOptionDto> services
) {
}
