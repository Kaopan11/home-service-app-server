package com.team.home_service_app_server.service;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team.home_service_app_server.dto.AdminServiceDto;
import com.team.home_service_app_server.dto.CreateServiceRequest;
import com.team.home_service_app_server.dto.PatchServiceRequest;
import com.team.home_service_app_server.dto.ServiceOptionDto;
import com.team.home_service_app_server.dto.ServiceOptionRequest;
import com.team.home_service_app_server.entity.Category;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceOptionItem;
import com.team.home_service_app_server.repository.CategoryRepository;
import com.team.home_service_app_server.repository.ServiceItemRepository;
import com.team.home_service_app_server.repository.ServiceOptionRepository;

@Service
public class AdminCatalogService {

	private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
	private static final Pattern DATA_IMAGE = Pattern.compile(
			"^data:image/(png|jpe?g)(;charset=[^;]+)?;base64,(.+)$",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	private final ServiceItemRepository serviceItemRepository;
	private final ServiceOptionRepository serviceOptionRepository;
	private final CategoryRepository categoryRepository;
	private final UserService userService;

	public AdminCatalogService(
			ServiceItemRepository serviceItemRepository,
			ServiceOptionRepository serviceOptionRepository,
			CategoryRepository categoryRepository,
			UserService userService) {
		this.serviceItemRepository = serviceItemRepository;
		this.serviceOptionRepository = serviceOptionRepository;
		this.categoryRepository = categoryRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<AdminServiceDto> list() {
		userService.requireAdmin();
		return serviceItemRepository.findAllByActiveTrueOrderBySortOrderAsc().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public AdminServiceDto getById(Long id) {
		userService.requireAdmin();
		ServiceItem item = findOrThrow(id);
		if (!item.isActive()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		return toDto(item);
	}

	@Transactional
	public AdminServiceDto create(CreateServiceRequest request) {
		userService.requireAdmin();
		String name = requireName(request == null ? null : request.name());
		Category category = requireCategory(request == null ? null : request.categoryId());
		String imageUrl = requireImage(request == null ? null : request.imageUrl());
		List<ServiceOptionRequest> options = requireOptions(request == null ? null : request.options());

		ServiceItem item = new ServiceItem();
		item.setName(name);
		item.setCategory(category);
		item.setImageUrl(imageUrl);
		item.setSortOrder(resolveCreateSortOrder(request.displayOrder()));
		item = serviceItemRepository.save(item);
		saveOptions(item, options);
		return toDto(item);
	}

	@Transactional
	public AdminServiceDto update(Long id, PatchServiceRequest request) {
		userService.requireAdmin();
		ServiceItem item = findOrThrow(id);
		PatchServiceRequest patch = request == null ? new PatchServiceRequest(null, null, null, null, null) : request;
		if (patch.name() != null) {
			item.setName(requireName(patch.name()));
		}
		if (patch.categoryId() != null) {
			item.setCategory(requireCategory(patch.categoryId()));
		}
		if (patch.imageUrl() != null) {
			item.setImageUrl(requireImage(patch.imageUrl()));
		}
		if (patch.displayOrder() != null) {
			if (patch.displayOrder() < 1) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "display_order must be at least 1");
			}
			item.setSortOrder(patch.displayOrder());
		}
		item = serviceItemRepository.save(item);
		if (patch.options() != null) {
			saveOptions(item, requireOptions(patch.options()));
		}
		return toDto(item);
	}

	@Transactional
	public void delete(Long id) {
		userService.requireAdmin();
		ServiceItem item = findOrThrow(id);
		if (!item.isActive()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		item.setActive(false);
		serviceItemRepository.save(item);
	}

	@Transactional
	public List<AdminServiceDto> reorder(List<Long> ids) {
		userService.requireAdmin();
		if (ids == null || ids.isEmpty() || ids.stream().anyMatch(java.util.Objects::isNull)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "service ids are required");
		}
		if (ids.size() != new HashSet<>(ids).size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "service ids must be unique");
		}
		List<ServiceItem> current = serviceItemRepository.findAllByActiveTrueOrderBySortOrderAsc();
		if (current.size() != ids.size()
				|| !current.stream().map(ServiceItem::getId).collect(Collectors.toSet()).equals(new HashSet<>(ids))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "service ids must include every active service");
		}
		Map<Long, ServiceItem> byId = current.stream().collect(Collectors.toMap(ServiceItem::getId, item -> item));
		for (int index = 0; index < ids.size(); index++) {
			ServiceItem item = byId.get(ids.get(index));
			item.setSortOrder(index + 1);
		}
		serviceItemRepository.saveAll(current);
		return current.stream()
				.sorted((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()))
				.map(this::toDto)
				.toList();
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
		String name = rawName.trim();
		if (name.length() > 255) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must be at most 255 characters");
		}
		return name;
	}

	private String requireImage(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image is required");
		}
		if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
			return imageUrl;
		}
		Matcher matcher = DATA_IMAGE.matcher(imageUrl.trim());
		if (!matcher.matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image must be PNG or JPG");
		}
		try {
			String payload = matcher.group(3).replaceAll("\\s", "");
			if (Base64.getDecoder().decode(payload).length > MAX_IMAGE_BYTES) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image must not exceed 5 MB");
			}
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image data is invalid");
		}
		return imageUrl;
	}

	private List<ServiceOptionRequest> requireOptions(List<ServiceOptionRequest> options) {
		if (options == null || options.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one service option is required");
		}
		for (ServiceOptionRequest option : options) {
			if (option == null || option.name() == null || option.name().isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option name is required");
			}
			if (option.name().trim().length() > 255) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option name must be at most 255 characters");
			}
			if (option.price() == null || option.price().signum() <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option price must be greater than zero");
			}
			if (option.displayOrder() != null && option.displayOrder() < 1) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option display_order must be at least 1");
			}
			if (option.unit() == null || option.unit().isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option unit is required");
			}
			if (option.unit().trim().length() > 255) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "option unit must be at most 255 characters");
			}
		}
		return options;
	}

	private void saveOptions(ServiceItem item, List<ServiceOptionRequest> options) {
		if (item.getId() != null) {
			serviceOptionRepository.deleteByService_Id(item.getId());
		}
		for (int index = 0; index < options.size(); index++) {
			ServiceOptionRequest requestOption = options.get(index);
			ServiceOptionItem option = new ServiceOptionItem();
			option.setService(item);
			option.setName(requestOption.name().trim());
			option.setPrice(requestOption.price());
			option.setUnit(requestOption.unit().trim());
			option.setSortOrder(requestOption.displayOrder() != null ? requestOption.displayOrder() : index + 1);
			serviceOptionRepository.save(option);
		}
	}

	private int resolveCreateSortOrder(Integer requested) {
		if (requested != null) {
			if (requested < 1) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "display_order must be at least 1");
			}
			return requested;
		}
		return nextSortOrder();
	}

	private int nextSortOrder() {
		return serviceItemRepository.findAllByActiveTrueOrderBySortOrderAsc().stream()
				.map(ServiceItem::getSortOrder)
				.filter(java.util.Objects::nonNull)
				.max(Integer::compareTo)
				.orElse(0) + 1;
	}

	private AdminServiceDto toDto(ServiceItem item) {
		List<ServiceOptionDto> options = item.getId() == null
				? List.of()
				: serviceOptionRepository.findByService_IdOrderBySortOrderAscIdAsc(item.getId()).stream()
						.map(option -> new ServiceOptionDto(
								option.getId(),
								option.getName(),
								option.getUnit(),
								option.getPrice(),
								option.getSortOrder()))
						.toList();
		return new AdminServiceDto(
				item.getId(),
				item.getSortOrder(),
				item.getName(),
				item.getCategory().getCategoryId(),
				item.getCategory().getName(),
				null,
				item.getImageUrl(),
				options,
				item.getCreatedAt(),
				item.getUpdatedAt());
	}
}
