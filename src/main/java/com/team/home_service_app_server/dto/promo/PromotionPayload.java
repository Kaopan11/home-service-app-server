package com.team.home_service_app_server.dto.promo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromotionPayload(
		String code,
		@JsonProperty("discount_type") String discountType,
		@JsonProperty("discount_value") BigDecimal discountValue,
		@JsonProperty("quota_limit") Integer quotaLimit,
		@JsonProperty("expires_at") String expiresAt
) {
}
