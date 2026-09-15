package com.team.home_service_app_server.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team.home_service_app_server.dto.AdminServiceDto;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.repository.ServiceItemRepository;

@Service
public class AdminCatalogService {

	private final ServiceItemRepository serviceItemRepository;
	private final UserService userService;

	public AdminCatalogService(ServiceItemRepository serviceItemRepository, UserService userService) {
		this.serviceItemRepository = serviceItemRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<AdminServiceDto> list() {
		userService.requireAdmin();
		return serviceItemRepository.findAllByOrderBySortOrderAsc().stream().map(this::toDto).toList();
	}

	@Transactional
	public void delete(Long id) {
		userService.requireAdmin();
		if (!serviceItemRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		serviceItemRepository.deleteById(id);
	}

	private AdminServiceDto toDto(ServiceItem item) {
		return new AdminServiceDto(
				item.getId(),
				item.getSortOrder(),
				item.getName(),
				item.getCategory().getName(),
				item.getCategory().getTone(),
				item.getCreatedAt(),
				item.getUpdatedAt());
	}
}
