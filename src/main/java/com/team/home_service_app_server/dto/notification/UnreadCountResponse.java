package com.team.home_service_app_server.dto.notification;

public record UnreadCountResponse(String message, long data) {

	public static UnreadCountResponse success(long data) {
		return new UnreadCountResponse("Success", data);
	}
}
