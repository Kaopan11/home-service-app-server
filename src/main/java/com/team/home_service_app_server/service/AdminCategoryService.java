package com.team.home_service_app_server.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.category.CategoryDto;
import com.team.home_service_app_server.dto.category.CreateCategoryRequest;
import com.team.home_service_app_server.dto.category.PatchCategoryRequest;
import com.team.home_service_app_server.entity.Category;
import com.team.home_service_app_server.exception.CategoryNameConflictException;
import com.team.home_service_app_server.exception.CategoryNotFoundException;
import com.team.home_service_app_server.exception.CategoryValidationException;
import com.team.home_service_app_server.repository.CategoryRepository;

@Service
public class AdminCategoryService {

	private static final int NAME_MAX_LENGTH = 255;

	private final CategoryRepository categoryRepository;
	private final UserService userService;

	public AdminCategoryService(CategoryRepository categoryRepository, UserService userService) {
		this.categoryRepository = categoryRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<CategoryDto> list() {
		userService.requireAdmin();
		return categoryRepository.findAllByOrderBySortOrderAscCreatedAtAsc().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public CategoryDto getById(int id) {
		userService.requireAdmin();
		return toDto(findOrThrow(id));
	}

	@Transactional
	public CategoryDto create(CreateCategoryRequest request) {
		userService.requireAdmin();
		String name = requireName(request == null ? null : request.name());
		assertNameAvailable(name, null);
		Category category = new Category();
		category.setName(name);
		category.setActive(true);
		category.setSortOrder(nextSortOrder());
		return toDto(categoryRepository.save(category));
	}

	@Transactional
	public CategoryDto update(int id, PatchCategoryRequest request) {
		userService.requireAdmin();
		Category category = findOrThrow(id);
		PatchCategoryRequest patch = request == null ? new PatchCategoryRequest(null, null) : request;
		if (patch.name() != null) {
			String name = requireName(patch.name());
			assertNameAvailable(name, category.getCategoryId());
			category.setName(name);
		}
		if (patch.active() != null) {
			category.setActive(patch.active());
		}
		return toDto(categoryRepository.save(category));
	}

	@Transactional
	public void delete(int id) {
		userService.requireAdmin();
		Category category = findOrThrow(id);
		if (category.isActive()) {
			category.setActive(false);
			categoryRepository.save(category);
		}
	}

	@Transactional
	public List<CategoryDto> reorder(List<Integer> ids) {
		userService.requireAdmin();
		if (ids == null || ids.isEmpty() || ids.stream().anyMatch(Objects::isNull)) {
			throw new CategoryValidationException("category ids are required");
		}
		if (ids.size() != new HashSet<>(ids).size()) {
			throw new CategoryValidationException("category ids must be unique");
		}
		List<Category> current = categoryRepository.findAllByActiveTrueOrderBySortOrderAscCreatedAtAsc();
		if (current.size() != ids.size()
				|| !current.stream().map(Category::getCategoryId).collect(Collectors.toSet()).equals(new HashSet<>(ids))) {
			throw new CategoryValidationException("category ids must include every active category");
		}
		Map<Integer, Category> byId = current.stream().collect(Collectors.toMap(Category::getCategoryId, item -> item));
		for (int index = 0; index < ids.size(); index++) {
			byId.get(ids.get(index)).setSortOrder(index + 1);
		}
		categoryRepository.saveAll(current);
		return categoryRepository.findAllByOrderBySortOrderAscCreatedAtAsc().stream().map(this::toDto).toList();
	}

	private Category findOrThrow(int id) {
		return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
	}

	private String requireName(String rawName) {
		if (rawName == null) {
			throw new CategoryValidationException("name is required");
		}
		String name = rawName.trim();
		if (name.isEmpty()) {
			throw new CategoryValidationException("name is required");
		}
		if (name.length() > NAME_MAX_LENGTH) {
			throw new CategoryValidationException("name must be at most 255 characters");
		}
		return name;
	}

	private int nextSortOrder() {
		return categoryRepository.findAllByActiveTrueOrderBySortOrderAscCreatedAtAsc().stream()
				.map(Category::getSortOrder)
				.filter(Objects::nonNull)
				.max(Integer::compareTo)
				.orElse(0) + 1;
	}

	private void assertNameAvailable(String name, Integer currentId) {
		Optional<Category> existing = categoryRepository.findByName(name);
		if (existing.isEmpty()) {
			return;
		}
		Category other = existing.get();
		if (currentId != null && other.getCategoryId().equals(currentId)) {
			return;
		}
		if (other.isActive()) {
			throw new CategoryNameConflictException(
					"CATEGORY_NAME_EXISTS",
					"Category name already exists",
					other.getCategoryId(),
					true);
		}
		throw new CategoryNameConflictException(
				"CATEGORY_INACTIVE",
				"A category with this name was deleted. Restore it instead of creating a new one.",
				other.getCategoryId(),
				false);
	}

	private CategoryDto toDto(Category category) {
		return new CategoryDto(
				category.getCategoryId(),
				category.getName(),
				category.isActive(),
				category.getCreatedAt(),
				category.getUpdatedAt());
	}

}
