package com.team.home_service_app_server.exception;

public class PromotionCodeConflictException extends RuntimeException {

	private final long promotionId;

	public PromotionCodeConflictException(long promotionId) {
		super("Promotion Code นี้มีอยู่แล้ว");
		this.promotionId = promotionId;
	}

	public long getPromotionId() {
		return promotionId;
	}
}
