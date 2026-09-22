package com.team.home_service_app_server.dto.notification;

public record NotificationResponse(String message, NotificationDto data) {

	public static NotificationResponse success(NotificationDto data) {
		return new NotificationResponse("Success", data);
	}
}
