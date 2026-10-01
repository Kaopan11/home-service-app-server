package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.notification.NotificationListResponse;
import com.team.home_service_app_server.dto.notification.NotificationResponse;
import com.team.home_service_app_server.dto.notification.UnreadCountResponse;
import com.team.home_service_app_server.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public NotificationListResponse list() {
		return NotificationListResponse.success(notificationService.list());
	}

	@GetMapping("/unread-count")
	public UnreadCountResponse unreadCount() {
		return UnreadCountResponse.success(notificationService.unreadCount());
	}

	@PatchMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable Long id) {
		return NotificationResponse.success(notificationService.markRead(id));
	}
}
