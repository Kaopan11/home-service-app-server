package com.team.home_service_app_server.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(name = "id", nullable = false, unique = true, updatable = false)
	private UUID publicId;

	@Column(name = "username", unique = true)
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "phone")
	private String phone;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "avatar_url", columnDefinition = "TEXT")
	private String avatarUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@Column(name = "address")
	private String address;

	@Column(name = "subdistrict")
	private String subdistrict;

	@Column(name = "district")
	private String district;

	@Column(name = "province")
	private String province;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

}