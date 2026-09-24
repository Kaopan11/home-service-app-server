package com.team.home_service_app_server.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class CustomerOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Long id;

	@Column(name = "order_code")
	private String orderCode;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User customer;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "total_price", nullable = false)
	private BigDecimal totalPrice;

	@Column(name = "scheduled_at")
	private Instant scheduledAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "customerOrder")
	private List<OrderItem> items = new ArrayList<>();

	@OneToMany(mappedBy = "customerOrder")
	private List<OrderAssignment> assignments = new ArrayList<>();
}
