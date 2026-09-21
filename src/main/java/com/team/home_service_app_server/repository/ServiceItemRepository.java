package com.team.home_service_app_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.home_service_app_server.entity.ServiceItem;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

	List<ServiceItem> findAllByOrderBySortOrderAsc();
}
