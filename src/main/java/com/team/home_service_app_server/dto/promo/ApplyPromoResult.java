package com.team.home_service_app_server.dto.promo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.home_service_app_server.entity.PromoDiscountType;

public record ApplyPromoResult(
		String code,
		@JsonProperty("discount_type") PromoDiscountType discountType,
		@JsonProperty("discount_value") BigDecimal discountValue,
		@JsonProperty("discount_amount") BigDecimal discountAmount,
		@JsonProperty("payable_amount") BigDecimal payableAmount
) {
}
