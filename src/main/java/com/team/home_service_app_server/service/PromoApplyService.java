package com.team.home_service_app_server.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.promo.ApplyPromoRequest;
import com.team.home_service_app_server.dto.promo.ApplyPromoResult;
import com.team.home_service_app_server.entity.PromoDiscountType;
import com.team.home_service_app_server.entity.Promotion;
import com.team.home_service_app_server.exception.PromotionNotFoundException;
import com.team.home_service_app_server.exception.PromotionValidationException;
import com.team.home_service_app_server.repository.PromotionRepository;

@Service
public class PromoApplyService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final PromotionRepository promotionRepository;

	public PromoApplyService(PromotionRepository promotionRepository) {
		this.promotionRepository = promotionRepository;
	}

	@Transactional(readOnly = true)
	public ApplyPromoResult apply(ApplyPromoRequest request) {
		if (request == null || request.code() == null || request.code().isBlank()) {
			throw new PromotionValidationException("กรุณากรอกรหัสโปรโมชัน");
		}
		if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) < 0) {
			throw new PromotionValidationException("ยอดชำระไม่ถูกต้อง");
		}

		Promotion promotion = promotionRepository.findByCodeIgnoreCase(request.code().trim())
				.orElseThrow(PromotionNotFoundException::new);

		if (!Promotion.DEFAULT_STATUS.equalsIgnoreCase(promotion.getStatus())) {
			throw new PromotionNotFoundException();
		}

		Instant expiresAt = promotion.getExpiresAt();
		if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
			throw new PromotionValidationException("รหัสโปรโมชันหมดอายุแล้ว");
		}

		int used = promotion.getQuotaUsed() == null ? 0 : promotion.getQuotaUsed();
		int limit = promotion.getQuotaLimit() == null ? 0 : promotion.getQuotaLimit();
		if (used >= limit) {
			throw new PromotionValidationException("รหัสโปรโมชันใช้ครบโควตาแล้ว");
		}

		BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
		BigDecimal discountAmount = discountAmount(promotion, amount);
		BigDecimal payable = amount.subtract(discountAmount);
		// ponytail: Omise/createCharge rejects < 1 baht; keep 1 until free checkout exists
		if (payable.compareTo(BigDecimal.ONE) < 0 && amount.compareTo(BigDecimal.ONE) >= 0) {
			payable = BigDecimal.ONE.setScale(2);
			discountAmount = amount.subtract(payable);
		}

		return new ApplyPromoResult(
				promotion.getCode(),
				promotion.getDiscountType(),
				promotion.getDiscountValue(),
				discountAmount,
				payable);
	}

	static BigDecimal discountAmount(Promotion promotion, BigDecimal amount) {
		BigDecimal value = promotion.getDiscountValue() == null ? BigDecimal.ZERO : promotion.getDiscountValue();
		PromoDiscountType type = promotion.getDiscountType() == null ? PromoDiscountType.FIXED : promotion.getDiscountType();
		BigDecimal raw = type == PromoDiscountType.PERCENT
				? amount.multiply(value).divide(HUNDRED, 2, RoundingMode.HALF_UP)
				: value.setScale(2, RoundingMode.HALF_UP);
		if (raw.compareTo(amount) > 0) {
			return amount;
		}
		if (raw.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO.setScale(2);
		}
		return raw;
	}
}
