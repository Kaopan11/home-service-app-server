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

import com.team.home_service_app_server.dto.promo.PromotionApiResponse;
import com.team.home_service_app_server.dto.promo.PromotionPayload;
import com.team.home_service_app_server.exception.PromotionValidationException;
import com.team.home_service_app_server.service.AdminPromotionService;

@RestController
@RequestMapping("/api/admin/promotions")
public class AdminPromotionController {

	private final AdminPromotionService adminPromotionService;

	public AdminPromotionController(AdminPromotionService adminPromotionService) {
		this.adminPromotionService = adminPromotionService;
	}

	@GetMapping
	public PromotionApiResponse list() {
		return PromotionApiResponse.list(adminPromotionService.list());
	}

	@GetMapping("/{id}")
	public PromotionApiResponse getById(@PathVariable String id) {
		return PromotionApiResponse.item(adminPromotionService.getById(parseId(id)));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PromotionApiResponse create(@RequestBody(required = false) PromotionPayload request) {
		return PromotionApiResponse.created(adminPromotionService.create(request));
	}

	@PatchMapping("/{id}")
	public PromotionApiResponse update(
			@PathVariable String id,
			@RequestBody(required = false) PromotionPayload request) {
		return PromotionApiResponse.updated(adminPromotionService.update(parseId(id), request));
	}

	@DeleteMapping("/{id}")
	public PromotionApiResponse delete(@PathVariable String id) {
		adminPromotionService.delete(parseId(id));
		return PromotionApiResponse.deleted();
	}

	private long parseId(String id) {
		try {
			return Long.parseLong(id);
		} catch (NumberFormatException exception) {
			throw new PromotionValidationException("Invalid promotion ID format");
		}
	}
}
