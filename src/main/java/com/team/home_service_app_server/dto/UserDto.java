package com.team.home_service_app_server.dto;

public record UserDto(
		Long id,
		String email,
		String fullName,
		String displayName,
		String firstName,
		String lastName,
		String phone,
		String address,
		String avatarUrl,
		String role
) {
}
