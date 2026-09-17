package com.team.home_service_app_server.dto;

public record UpdateUserProfileRequest(
		String email,
		String displayName,
		String firstName,
		String lastName,
		String phone,
		String address,
		String subdistrict,
		String district,
		String province,
		String avatarUrl
) {
}