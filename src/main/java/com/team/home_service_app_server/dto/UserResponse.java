package com.team.home_service_app_server.dto;

public record UserResponse(String message, UserDto data) {

	public static UserResponse success(UserDto user) {
		return new UserResponse("Success", user);
	}

}
