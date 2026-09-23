package com.team.home_service_app_server.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.request.CreateChargeRequest;
import com.team.home_service_app_server.dto.response.ChargeResponse;
import com.team.home_service_app_server.service.PaymentService;

@RestController
@RequestMapping("/api/charges")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ChargeResponse create(@Valid @RequestBody CreateChargeRequest request) {
		return ChargeResponse.success(paymentService.charge(
				request.token(),
				request.amount(),
				request.description()));
	}

}
