package com.team.home_service_app_server.dto.promo;

import java.math.BigDecimal;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.team.home_service_app_server.entity.PromoDiscountType;

public record PromotionDto(
		@JsonProperty("promotion_id") Long promotionId,
		String code,
		@JsonProperty("discount_type") PromoDiscountType discountType,
		@JsonProperty("discount_value") BigDecimal discountValue,
		@JsonProperty("quota_limit") Integer quotaLimit,
		@JsonProperty("quota_used") Integer quotaUsed,
		@JsonProperty("expires_at") @JsonFormat(
				shape = JsonFormat.Shape.STRING,
				pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				timezone = "UTC") Instant expiresAt,
		@JsonProperty("created_at") @JsonFormat(
				shape = JsonFormat.Shape.STRING,
				pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				timezone = "UTC") Instant createdAt,
		@JsonProperty("updated_at") @JsonFormat(
				shape = JsonFormat.Shape.STRING,
				pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				timezone = "UTC") Instant updatedAt
) {
}
