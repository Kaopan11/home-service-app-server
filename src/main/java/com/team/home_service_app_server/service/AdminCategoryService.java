package com.team.home_service_app_server.service;

import java.util.List;
import java.util.Optional;

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
		return categoryRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
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
