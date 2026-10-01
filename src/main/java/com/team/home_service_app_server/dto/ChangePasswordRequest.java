package com.team.home_service_app_server.dto;

public record ChangePasswordRequest(
		String currentPassword,
		String newPassword,
		String confirmNewPassword,
		String confirmPassword
) {

	public String confirm() {
		return confirmNewPassword != null ? confirmNewPassword : confirmPassword;
	}

}
