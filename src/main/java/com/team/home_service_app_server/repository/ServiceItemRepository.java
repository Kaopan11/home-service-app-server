package com.team.home_service_app_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.home_service_app_server.entity.ServiceItem;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

	List<ServiceItem> findAllByActiveTrueOrderBySortOrderAsc();

	@Query("select s from ServiceItem s join fetch s.category where s.active = true order by s.sortOrder asc")
	List<ServiceItem> findAllWithCategoryOrderBySortOrderAsc();

	@Query("select s from ServiceItem s join fetch s.category where s.id = :id")
	Optional<ServiceItem> findWithCategoryById(@Param("id") Long id);
}
