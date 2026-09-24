package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.OrderListResponse;
import com.team.home_service_app_server.service.CustomerOrderService;

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
}
