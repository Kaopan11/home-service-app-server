package com.team.home_service_app_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.team.home_service_app_server.dto.promo.ApplyPromoRequest;
import com.team.home_service_app_server.dto.promo.ApplyPromoResult;
import com.team.home_service_app_server.entity.PromoDiscountType;
import com.team.home_service_app_server.entity.Promotion;
import com.team.home_service_app_server.exception.PromotionNotFoundException;
import com.team.home_service_app_server.exception.PromotionValidationException;
import com.team.home_service_app_server.repository.PromotionRepository;

class PromoApplyServiceTest {

	@Test
	void percentDiscountReducesPayable() {
		Promotion promotion = active("SAVE10", PromoDiscountType.PERCENT, "10", 10, 0, Instant.now().plusSeconds(3600));
		PromoApplyService service = serviceWith(promotion);

		ApplyPromoResult result = service.apply(new ApplyPromoRequest("save10", new BigDecimal("1000")));

		assertEquals(new BigDecimal("100.00"), result.discountAmount());
		assertEquals(new BigDecimal("900.00"), result.payableAmount());
	}

	@Test
	void rejectsMissingExpiredAndQuotaFull() {
		PromotionRepository repo = mock(PromotionRepository.class);
		when(repo.findByCodeIgnoreCase("NONE")).thenReturn(Optional.empty());
		PromoApplyService service = new PromoApplyService(repo);
		assertThrows(PromotionNotFoundException.class, () -> service.apply(new ApplyPromoRequest("NONE", BigDecimal.TEN)));

		Promotion expired = active("OLD", PromoDiscountType.FIXED, "50", 10, 0, Instant.EPOCH);
		when(repo.findByCodeIgnoreCase("OLD")).thenReturn(Optional.of(expired));
		assertThrows(PromotionValidationException.class, () -> service.apply(new ApplyPromoRequest("OLD", BigDecimal.TEN)));

		Promotion full = active("FULL", PromoDiscountType.FIXED, "50", 5, 5, Instant.now().plusSeconds(3600));
		when(repo.findByCodeIgnoreCase("FULL")).thenReturn(Optional.of(full));
		assertThrows(PromotionValidationException.class, () -> service.apply(new ApplyPromoRequest("FULL", BigDecimal.TEN)));
	}

	private static PromoApplyService serviceWith(Promotion promotion) {
		PromotionRepository repo = mock(PromotionRepository.class);
		when(repo.findByCodeIgnoreCase(anyString())).thenReturn(Optional.of(promotion));
		return new PromoApplyService(repo);
	}

	private static Promotion active(
			String code,
			PromoDiscountType type,
			String discount,
			int quota,
			int used,
			Instant expiresAt) {
		Promotion promotion = new Promotion();
		promotion.setCode(code);
		promotion.setStatus(Promotion.DEFAULT_STATUS);
		promotion.setDiscountType(type);
		promotion.setDiscountValue(new BigDecimal(discount));
		promotion.setQuotaLimit(quota);
		promotion.setQuotaUsed(used);
		promotion.setExpiresAt(expiresAt);
		return promotion;
	}
}
