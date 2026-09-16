package com.team.home_service_app_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.TechnicianProfile;

public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {

	Optional<TechnicianProfile> findByUser_UserId(Long userId);

}
