package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.OrderListResponse;
import com.team.home_service_app_server.dto.request.SubmitReviewRequest;
import com.team.home_service_app_server.service.CustomerOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

	private final CustomerOrderService customerOrderService;

	public CustomerOrderController(CustomerOrderService customerOrderService) {
		this.customerOrderService = customerOrderService;
	}

	@GetMapping
	public OrderListResponse list(@RequestParam(defaultValue = "active") String scope) {
		return OrderListResponse.success(customerOrderService.listCurrentUser(scope));
	}

	@PostMapping("/{jobId}/review")
	public OrderListResponse review(@PathVariable Long jobId, @Valid @RequestBody SubmitReviewRequest request) {
		return OrderListResponse.success(java.util.List.of(customerOrderService.submitReview(jobId, request)));
	}
}
