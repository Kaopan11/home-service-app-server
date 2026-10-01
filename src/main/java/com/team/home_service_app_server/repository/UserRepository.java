package com.team.home_service_app_server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

	boolean existsByPhoneAndUserIdNot(String phone, Long userId);

	Optional<User> findByEmail(String email);

	Optional<User> findByPublicId(UUID publicId);

	List<User> findByRole(UserRole role);

}
