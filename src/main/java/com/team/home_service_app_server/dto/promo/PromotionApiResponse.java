package com.team.home_service_app_server.dto.promo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PromotionApiResponse(
		boolean success,
		String message,
		Object data,
		String code,
		List<PromotionFieldError> errors
) {

	public record PromotionFieldError(String message) {
	}

	public record ConflictData(@JsonProperty("promotion_id") Long promotionId) {
	}

	public static PromotionApiResponse list(List<PromotionDto> promotions) {
		return new PromotionApiResponse(true, null, promotions, null, null);
	}

	public static PromotionApiResponse item(PromotionDto promotion) {
		return new PromotionApiResponse(true, null, promotion, null, null);
	}

	public static PromotionApiResponse created(PromotionDto promotion) {
		return new PromotionApiResponse(true, "Promotion created successfully", promotion, null, null);
	}

	public static PromotionApiResponse updated(PromotionDto promotion) {
		return new PromotionApiResponse(true, "Promotion updated successfully", promotion, null, null);
	}

	public static PromotionApiResponse deleted() {
		return new PromotionApiResponse(true, "Promotion deleted successfully", null, null, null);
	}

	public static PromotionApiResponse notFound() {
		return new PromotionApiResponse(false, "Promotion not found", null, null, null);
	}

	public static PromotionApiResponse validation(String message) {
		return new PromotionApiResponse(false, null, null, null, List.of(new PromotionFieldError(message)));
	}

	public static PromotionApiResponse conflict(String code, String message, ConflictData data) {
		return new PromotionApiResponse(false, message, data, code, null);
	}
}
