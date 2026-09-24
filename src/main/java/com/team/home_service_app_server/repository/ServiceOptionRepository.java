package com.team.home_service_app_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.team.home_service_app_server.entity.ServiceOptionItem;

public interface ServiceOptionRepository extends JpaRepository<ServiceOptionItem, Long> {

	List<ServiceOptionItem> findByService_IdOrderByIdAsc(Long serviceId);

	@Query("select o.service.id, min(o.price) from ServiceOptionItem o group by o.service.id")
	List<Object[]> findMinPriceByServiceId();
}
