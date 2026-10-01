package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.promo.ApplyPromoRequest;
import com.team.home_service_app_server.dto.promo.PromotionApiResponse;
import com.team.home_service_app_server.service.PromoApplyService;

@RestController
@RequestMapping("/api/promotions")
public class PromoApplyController {

	private final PromoApplyService promoApplyService;

	public PromoApplyController(PromoApplyService promoApplyService) {
		this.promoApplyService = promoApplyService;
	}

	@PostMapping("/apply")
	public PromotionApiResponse apply(@RequestBody(required = false) ApplyPromoRequest request) {
		return PromotionApiResponse.applied(promoApplyService.apply(request));
	}
}
