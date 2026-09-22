package com.team.home_service_app_server.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team.home_service_app_server.dto.notification.NotificationDto;
import com.team.home_service_app_server.entity.Notification;
import com.team.home_service_app_server.entity.NotificationType;
import com.team.home_service_app_server.entity.ServiceJob;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.repository.NotificationRepository;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final UserService userService;

	public NotificationService(NotificationRepository notificationRepository, UserService userService) {
		this.notificationRepository = notificationRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<NotificationDto> list() {
		User user = userService.requireCurrentUserEntity();
		return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId()).stream()
				.map(this::toDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public long unreadCount() {
		User user = userService.requireCurrentUserEntity();
		return notificationRepository.countByUser_UserIdAndReadAtIsNull(user.getUserId());
	}

	@Transactional
	public NotificationDto markRead(Long id) {
		User user = userService.requireCurrentUserEntity();
		Notification notification = notificationRepository.findById(id)
				.filter(item -> item.getUser().getUserId().equals(user.getUserId()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		if (notification.getReadAt() == null) {
			notification.setReadAt(Instant.now());
			notification = notificationRepository.save(notification);
		}
		return toDto(notification);
	}

	/** Creates a notification for a recipient. Called from job-status transitions, not exposed as an API. */
	@Transactional
	public void notify(User recipient, NotificationType type, String title, String body, ServiceJob job) {
		Notification notification = new Notification();
		notification.setUser(recipient);
		notification.setType(type);
		notification.setTitle(title);
		notification.setBody(body);
		notification.setJob(job);
		notificationRepository.save(notification);
	}

	private NotificationDto toDto(Notification notification) {
		return new NotificationDto(
				notification.getId(),
				notification.getType().name(),
				notification.getTitle(),
				notification.getBody(),
				notification.getJob() != null ? notification.getJob().getId() : null,
				notification.getReadAt() != null,
				notification.getCreatedAt());
	}
}
