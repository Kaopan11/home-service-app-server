package com.team.home_service_app_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.category.CategoryApiResponse;
import com.team.home_service_app_server.dto.category.CreateCategoryRequest;
import com.team.home_service_app_server.dto.category.PatchCategoryRequest;
import com.team.home_service_app_server.dto.category.ReorderCategoriesRequest;
import com.team.home_service_app_server.exception.CategoryValidationException;
import com.team.home_service_app_server.service.AdminCategoryService;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

	private final AdminCategoryService adminCategoryService;

	public AdminCategoryController(AdminCategoryService adminCategoryService) {
		this.adminCategoryService = adminCategoryService;
	}

	@GetMapping
	public CategoryApiResponse list() {
		return CategoryApiResponse.list(adminCategoryService.list());
	}

	@PatchMapping
	public CategoryApiResponse reorder(@RequestBody(required = false) ReorderCategoriesRequest request) {
		return CategoryApiResponse.list(adminCategoryService.reorder(request == null ? null : request.ids()));
	}

	@GetMapping("/{id}")
	public CategoryApiResponse getById(@PathVariable String id) {
		return CategoryApiResponse.item(adminCategoryService.getById(parseId(id)));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryApiResponse create(@RequestBody(required = false) CreateCategoryRequest request) {
		return CategoryApiResponse.created(adminCategoryService.create(request));
	}

	@PatchMapping("/{id}")
	public CategoryApiResponse update(
			@PathVariable String id,
			@RequestBody(required = false) PatchCategoryRequest request) {
		return CategoryApiResponse.updated(adminCategoryService.update(parseId(id), request));
	}

	@DeleteMapping("/{id}")
	public CategoryApiResponse delete(@PathVariable String id) {
		adminCategoryService.delete(parseId(id));
		return CategoryApiResponse.deleted();
	}

	private int parseId(String id) {
		try {
			return Integer.parseInt(id);
		} catch (NumberFormatException exception) {
			throw new CategoryValidationException("Invalid category ID format");
		}
	}

}
