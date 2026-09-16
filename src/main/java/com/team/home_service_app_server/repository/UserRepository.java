package com.team.home_service_app_server.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	boolean existsByPhone(String phone);

	Optional<User> findByEmail(String email);

	Optional<User> findByPublicId(UUID publicId);

}
