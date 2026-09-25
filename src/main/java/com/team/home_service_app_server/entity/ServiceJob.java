package com.team.home_service_app_server.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "service_jobs")
@Getter
@Setter
public class ServiceJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "job_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private User customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "technician_id")
	private User technician;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "service_id", nullable = false)
	private ServiceItem service;

	@Column(name = "address", nullable = false, columnDefinition = "text")
	private String address;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private JobStatus status = JobStatus.WAITING_ACCEPT;

	@Column(name = "order_code", length = 32)
	private String orderCode;

	@Column(name = "total_price", precision = 12, scale = 2)
	private java.math.BigDecimal totalPrice;

	@Column(name = "scheduled_at")
	private Instant scheduledAt;

	@Column(name = "items_description", columnDefinition = "text")
	private String itemsDescription;

	@Column(name = "rating")
	private Integer rating;

	@Column(name = "review_comment", columnDefinition = "text")
	private String reviewComment;

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
		if (status == null) {
			status = JobStatus.WAITING_ACCEPT;
		}
		if (orderCode == null || orderCode.isBlank()) {
			orderCode = "AD" + String.format("%08d", (System.currentTimeMillis() % 100000000L));
		}
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
