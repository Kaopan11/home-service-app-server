package com.team.home_service_app_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

	List<Promotion> findAllByOrderByCreatedAtDesc();

	Optional<Promotion> findByCodeIgnoreCase(String code);
}
