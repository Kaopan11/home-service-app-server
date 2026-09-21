package com.team.home_service_app_server.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.team.home_service_app_server.entity.PromoDiscountType;
import com.team.home_service_app_server.entity.Promotion;
import com.team.home_service_app_server.repository.PromotionRepository;

@Configuration
public class PromotionSeedConfig {

	@Bean
	CommandLineRunner seedPromotions(PromotionRepository promotions) {
		return args -> {
			if (promotions.count() > 0) {
				return;
			}

			Instant created = Instant.parse("2022-02-12T22:30:00Z");
			Instant expires = Instant.parse("2022-06-12T22:30:00Z");

			promotions.saveAll(List.of(
					promo("HOME0202", PromoDiscountType.FIXED, "50", 100, 10, created, expires),
					promo("HOME10", PromoDiscountType.PERCENT, "10", 100, 5, created, expires),
					promo("HOME50", PromoDiscountType.FIXED, "50", 100, 10, created, expires),
					promo("HOME15", PromoDiscountType.FIXED, "50", 100, 10, created, expires),
					promo("HOME20", PromoDiscountType.FIXED, "50", 100, 10, created, expires)));
		};
	}

	private static Promotion promo(
			String code,
			PromoDiscountType type,
			String value,
			int quotaLimit,
			int quotaUsed,
			Instant created,
			Instant expires) {
		Promotion promotion = new Promotion();
		promotion.setCode(code);
		promotion.setStatus(Promotion.DEFAULT_STATUS);
		promotion.setDiscountType(type);
		promotion.setDiscountValue(new BigDecimal(value));
		promotion.setQuotaLimit(quotaLimit);
		promotion.setQuotaUsed(quotaUsed);
		promotion.setExpiresAt(expires);
		promotion.setCreatedAt(created);
		promotion.setUpdatedAt(created);
		return promotion;
	}
}
