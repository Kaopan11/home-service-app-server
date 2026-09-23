package com.team.home_service_app_server.dto.notification;

import java.util.List;

public record NotificationListResponse(String message, List<NotificationDto> data) {

	public static NotificationListResponse success(List<NotificationDto> data) {
		return new NotificationListResponse("Success", data);
	}
}
