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
		String subdistrict,
		String district,
		String province,
		String avatarUrl,
		String role
) {
	public UserDto(
			Long id,
			String email,
			String fullName,
			String displayName,
			String firstName,
			String lastName,
			String phone,
			String address,
			String avatarUrl,
			String role) {
		this(id, email, fullName, displayName, firstName, lastName, phone, address, null, null, null, avatarUrl, role);
	}
}