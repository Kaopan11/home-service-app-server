package com.team.home_service_app_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
