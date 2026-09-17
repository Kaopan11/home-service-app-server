package com.team.home_service_app_server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.ServiceListItemDto;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.repository.ServiceItemRepository;

@Service
public class CatalogService {

	private final ServiceItemRepository serviceItemRepository;

	public CatalogService(ServiceItemRepository serviceItemRepository) {
		this.serviceItemRepository = serviceItemRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceListItemDto> list() {
		return serviceItemRepository.findAllByOrderBySortOrderAsc().stream().map(this::toDto).toList();
	}

	private ServiceListItemDto toDto(ServiceItem item) {
		return new ServiceListItemDto(
				item.getId(),
				item.getName(),
				item.getCategory().getName(),
				item.getSortOrder());
	}
}
