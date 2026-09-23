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

import com.team.home_service_app_server.dto.AdminServiceListResponse;
import com.team.home_service_app_server.dto.AdminServiceResponse;
import com.team.home_service_app_server.dto.CreateServiceRequest;
import com.team.home_service_app_server.dto.PatchServiceRequest;
import com.team.home_service_app_server.service.AdminCatalogService;

@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {

	private final AdminCatalogService adminCatalogService;

	public AdminServiceController(AdminCatalogService adminCatalogService) {
		this.adminCatalogService = adminCatalogService;
	}

	@GetMapping
	public AdminServiceListResponse list() {
		return AdminServiceListResponse.success(adminCatalogService.list());
	}

	@GetMapping("/{id}")
	public AdminServiceResponse getById(@PathVariable Long id) {
		return AdminServiceResponse.success(adminCatalogService.getById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AdminServiceResponse create(@RequestBody(required = false) CreateServiceRequest request) {
		return AdminServiceResponse.success(adminCatalogService.create(request));
	}

	@PatchMapping("/{id}")
	public AdminServiceResponse update(@PathVariable Long id, @RequestBody(required = false) PatchServiceRequest request) {
		return AdminServiceResponse.success(adminCatalogService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		adminCatalogService.delete(id);
	}
}
