package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.ServiceDetailResponse;
import com.team.home_service_app_server.dto.ServiceListResponse;
import com.team.home_service_app_server.service.CatalogService;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

	private final CatalogService catalogService;

	public ServiceController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@GetMapping
	public ServiceListResponse list() {
		return ServiceListResponse.success(catalogService.list());
	}

	@GetMapping("/{id}")
	public ServiceDetailResponse getById(@PathVariable long id) {
		return ServiceDetailResponse.success(catalogService.getById(id));
	}
}
