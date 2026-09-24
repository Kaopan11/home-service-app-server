package com.team.home_service_app_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

	List<Category> findAllByOrderBySortOrderAscCreatedAtAsc();

	List<Category> findAllByActiveTrueOrderBySortOrderAscCreatedAtAsc();

	Optional<Category> findByName(String name);

}
