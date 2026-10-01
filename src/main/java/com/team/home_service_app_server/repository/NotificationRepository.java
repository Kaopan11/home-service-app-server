package com.team.home_service_app_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

	long countByUser_UserIdAndReadAtIsNull(Long userId);
}
