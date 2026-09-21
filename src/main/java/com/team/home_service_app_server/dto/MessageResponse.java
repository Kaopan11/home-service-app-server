package com.team.home_service_app_server.dto;

public record MessageResponse(String message) {

	public static MessageResponse logoutSuccess() {
		return new MessageResponse("ออกจากระบบสำเร็จ");
	}

}
