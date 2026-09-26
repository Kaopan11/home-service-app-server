package com.team.home_service_app_server.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.request.CreateChargeRequest;
import com.team.home_service_app_server.dto.response.ChargeResponse;
import com.team.home_service_app_server.service.CustomerOrderService;
import com.team.home_service_app_server.service.CustomerOrderService.PaidBooking;
import com.team.home_service_app_server.service.PaymentService;

@RestController
@RequestMapping("/api/charges")
public class PaymentController {

	private final PaymentService paymentService;
	private final CustomerOrderService customerOrderService;

	public PaymentController(PaymentService paymentService, CustomerOrderService customerOrderService) {
		this.paymentService = paymentService;
		this.customerOrderService = customerOrderService;
	}

	@PostMapping
	public ChargeResponse create(@Valid @RequestBody CreateChargeRequest request) {
		PaidBooking booking = customerOrderService.draft(request);
		ChargeResponse.ChargeData charge = paymentService.charge(
				request.token(),
				request.amount(),
				request.description());
		customerOrderService.savePaidBooking(booking, request.amount());
		return ChargeResponse.success(charge);
	}

}
