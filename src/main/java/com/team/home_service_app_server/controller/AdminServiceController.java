package com.team.home_service_app_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.AdminServiceListResponse;
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

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		adminCatalogService.delete(id);
	}
}
