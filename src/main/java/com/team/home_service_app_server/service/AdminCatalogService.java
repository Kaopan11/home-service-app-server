package com.team.home_service_app_server.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team.home_service_app_server.dto.AdminServiceDto;
import com.team.home_service_app_server.dto.CreateServiceRequest;
import com.team.home_service_app_server.dto.PatchServiceRequest;
import com.team.home_service_app_server.entity.Category;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.repository.CategoryRepository;
import com.team.home_service_app_server.repository.ServiceItemRepository;

@Service
public class AdminCatalogService {

	private final ServiceItemRepository serviceItemRepository;
	private final CategoryRepository categoryRepository;
	private final UserService userService;

	public AdminCatalogService(
			ServiceItemRepository serviceItemRepository,
			CategoryRepository categoryRepository,
			UserService userService) {
		this.serviceItemRepository = serviceItemRepository;
		this.categoryRepository = categoryRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<AdminServiceDto> list() {
		userService.requireAdmin();
		return serviceItemRepository.findAllByOrderBySortOrderAsc().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public AdminServiceDto getById(Long id) {
		userService.requireAdmin();
		return toDto(findOrThrow(id));
	}

	@Transactional
	public AdminServiceDto create(CreateServiceRequest request) {
		userService.requireAdmin();
		String name = requireName(request == null ? null : request.name());
		Category category = requireCategory(request == null ? null : request.categoryId());

		ServiceItem item = new ServiceItem();
		item.setName(name);
		item.setCategory(category);
		item.setSortOrder(nextSortOrder());
		return toDto(serviceItemRepository.save(item));
	}

	@Transactional
	public AdminServiceDto update(Long id, PatchServiceRequest request) {
		userService.requireAdmin();
		ServiceItem item = findOrThrow(id);
		PatchServiceRequest patch = request == null ? new PatchServiceRequest(null, null) : request;
		if (patch.name() != null) {
			item.setName(requireName(patch.name()));
		}
		if (patch.categoryId() != null) {
			item.setCategory(requireCategory(patch.categoryId()));
		}
		return toDto(serviceItemRepository.save(item));
	}

	@Transactional
	public void delete(Long id) {
		userService.requireAdmin();
		if (!serviceItemRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		serviceItemRepository.deleteById(id);
	}

	private ServiceItem findOrThrow(Long id) {
		return serviceItemRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	private Category requireCategory(Integer categoryId) {
		if (categoryId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category_id is required");
		}
		return categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
	}

	private String requireName(String rawName) {
		if (rawName == null || rawName.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
		}
		return rawName.trim();
	}

	private int nextSortOrder() {
		return serviceItemRepository.findAllByOrderBySortOrderAsc().stream()
				.map(ServiceItem::getSortOrder)
				.filter(java.util.Objects::nonNull)
				.max(Integer::compareTo)
				.orElse(0) + 1;
	}

	private AdminServiceDto toDto(ServiceItem item) {
		return new AdminServiceDto(
				item.getId(),
				item.getSortOrder(),
				item.getName(),
				item.getCategory().getCategoryId(),
				item.getCategory().getName(),
				null,
				item.getCreatedAt(),
				item.getUpdatedAt());
	}
}
