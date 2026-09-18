package com.team.home_service_app_server.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "promotions")
@Getter
@Setter
public class Promotion {

	public static final String DEFAULT_STATUS = "active";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "promotion_id")
	private Long promotionId;

	@Column(name = "promotion_code", nullable = false, unique = true, length = 50)
	private String code;

	@Column(name = "status", nullable = false)
	private String status = DEFAULT_STATUS;

	@Convert(converter = PromoDiscountTypeConverter.class)
	@Column(name = "type")
	private PromoDiscountType discountType;

	@Column(name = "discount", nullable = false, precision = 12, scale = 2)
	private BigDecimal discountValue;

	@Column(name = "quota", nullable = false)
	private Integer quotaLimit;

	@Column(name = "quota_used", nullable = false)
	private Integer quotaUsed = 0;

	@Column(name = "expire")
	private Instant expiresAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		if (updatedAt == null) {
			updatedAt = now;
		}
		if (quotaUsed == null) {
			quotaUsed = 0;
		}
		if (status == null || status.isBlank()) {
			status = DEFAULT_STATUS;
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
