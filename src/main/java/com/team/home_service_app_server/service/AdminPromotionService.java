package com.team.home_service_app_server.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.promo.PromotionDto;
import com.team.home_service_app_server.dto.promo.PromotionPayload;
import com.team.home_service_app_server.entity.PromoDiscountType;
import com.team.home_service_app_server.entity.Promotion;
import com.team.home_service_app_server.exception.PromotionCodeConflictException;
import com.team.home_service_app_server.exception.PromotionNotFoundException;
import com.team.home_service_app_server.exception.PromotionValidationException;
import com.team.home_service_app_server.repository.PromotionRepository;

@Service
public class AdminPromotionService {

	private static final int CODE_MAX_LENGTH = 50;

	private final PromotionRepository promotionRepository;
	private final UserService userService;

	public AdminPromotionService(PromotionRepository promotionRepository, UserService userService) {
		this.promotionRepository = promotionRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<PromotionDto> list() {
		userService.requireAdmin();
		return promotionRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public PromotionDto getById(long id) {
		userService.requireAdmin();
		return toDto(findOrThrow(id));
	}

	@Transactional
	public PromotionDto create(PromotionPayload request) {
		userService.requireAdmin();
		ValidatedPayload payload = validate(request);
		assertCodeAvailable(payload.code(), null);

		Promotion promotion = new Promotion();
		apply(promotion, payload);
		promotion.setStatus(Promotion.DEFAULT_STATUS);
		promotion.setQuotaUsed(0);
		return toDto(promotionRepository.save(promotion));
	}

	@Transactional
	public PromotionDto update(long id, PromotionPayload request) {
		userService.requireAdmin();
		Promotion promotion = findOrThrow(id);
		ValidatedPayload payload = validate(request);
		assertCodeAvailable(payload.code(), promotion.getPromotionId());

		apply(promotion, payload);
		int used = promotion.getQuotaUsed() == null ? 0 : promotion.getQuotaUsed();
		promotion.setQuotaUsed(Math.min(used, payload.quotaLimit()));
		return toDto(promotionRepository.save(promotion));
	}

	@Transactional
	public void delete(long id) {
		userService.requireAdmin();
		Promotion promotion = findOrThrow(id);
		promotionRepository.delete(promotion);
	}

	private Promotion findOrThrow(long id) {
		return promotionRepository.findById(id).orElseThrow(PromotionNotFoundException::new);
	}

	private void apply(Promotion promotion, ValidatedPayload payload) {
		promotion.setCode(payload.code());
		promotion.setDiscountType(payload.discountType());
		promotion.setDiscountValue(payload.discountValue());
		promotion.setQuotaLimit(payload.quotaLimit());
		promotion.setExpiresAt(payload.expiresAt());
	}

	private ValidatedPayload validate(PromotionPayload request) {
		if (request == null) {
			throw new PromotionValidationException("request body is required");
		}

		String code = requireCode(request.code());
		PromoDiscountType discountType = PromoDiscountType.fromJson(request.discountType());
		if (discountType == null) {
			throw new PromotionValidationException("discount_type must be fixed or percent");
		}

		BigDecimal discountValue = request.discountValue();
		if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
			throw new PromotionValidationException("discount_value must be greater than 0");
		}
		if (discountType == PromoDiscountType.PERCENT && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
			throw new PromotionValidationException("discount_value must be at most 100 for percent");
		}

		Integer quotaLimit = request.quotaLimit();
		if (quotaLimit == null || quotaLimit <= 0) {
			throw new PromotionValidationException("quota_limit must be an integer greater than 0");
		}

		Instant expiresAt = parseExpiresAt(request.expiresAt());
		return new ValidatedPayload(code, discountType, discountValue, quotaLimit, expiresAt);
	}

	private String requireCode(String rawCode) {
		if (rawCode == null) {
			throw new PromotionValidationException("code is required");
		}
		String code = rawCode.trim().toUpperCase();
		if (code.isEmpty()) {
			throw new PromotionValidationException("code is required");
		}
		if (code.length() > CODE_MAX_LENGTH) {
			throw new PromotionValidationException("code must be at most 50 characters");
		}
		return code;
	}

	private Instant parseExpiresAt(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new PromotionValidationException("expires_at is required");
		}
		String value = raw.trim();
		try {
			return Instant.parse(value);
		} catch (DateTimeParseException ignored) {
			try {
				return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
			} catch (DateTimeParseException exception) {
				throw new PromotionValidationException("expires_at is invalid");
			}
		}
	}

	private void assertCodeAvailable(String code, Long currentId) {
		Optional<Promotion> existing = promotionRepository.findByCodeIgnoreCase(code);
		if (existing.isEmpty()) {
			return;
		}
		Promotion other = existing.get();
		if (currentId != null && other.getPromotionId().equals(currentId)) {
			return;
		}
		throw new PromotionCodeConflictException(other.getPromotionId());
	}

	private PromotionDto toDto(Promotion promotion) {
		return new PromotionDto(
				promotion.getPromotionId(),
				promotion.getCode(),
				promotion.getDiscountType(),
				promotion.getDiscountValue(),
				promotion.getQuotaLimit(),
				promotion.getQuotaUsed(),
				promotion.getExpiresAt(),
				promotion.getCreatedAt(),
				promotion.getUpdatedAt());
	}

	private record ValidatedPayload(
			String code,
			PromoDiscountType discountType,
			BigDecimal discountValue,
			Integer quotaLimit,
			Instant expiresAt
	) {
	}
}
