package com.team.home_service_app_server.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.UpdateUserProfileRequest;
import com.team.home_service_app_server.dto.UserDto;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.entity.UserRole;
import com.team.home_service_app_server.exception.ForbiddenException;
import com.team.home_service_app_server.exception.UnauthorizedException;
import com.team.home_service_app_server.mapper.UserMapper;
import com.team.home_service_app_server.repository.UserRepository;
import com.team.home_service_app_server.security.JwtPrincipal;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public UserDto requireAdmin() {
		User user = requireCurrentUserEntity();
		if (user.getRole() != UserRole.ADMIN) {
			throw new ForbiddenException();
		}
		return userMapper.toDto(user);
	}

	public User requireCurrentTechnician() {
		User user = requireCurrentUserEntity();
		if (user.getRole() != UserRole.TECHNICIAN) {
			throw new ForbiddenException("บัญชีนี้ไม่มีสิทธิ์เข้าถึงระบบช่าง");
		}
		return user;
	}

	public UserDto getCurrentUser() {
		return userMapper.toDto(requireCurrentUserEntity());
	}

	public User requireCurrentUserEntity() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new UnauthorizedException();
		}

		return resolveUser(authentication).orElseThrow(UnauthorizedException::new);
	}

	private Optional<User> resolveUser(Authentication authentication) {
		JwtPrincipal principal = authentication.getDetails() instanceof JwtPrincipal jwtPrincipal
				? jwtPrincipal
				: new JwtPrincipal(authentication.getName(), null);

		Optional<User> byEmail = Optional.empty();
		if (principal.email() != null && !principal.email().isBlank()) {
			byEmail = userRepository.findByEmail(principal.email());
		} else {
			byEmail = userRepository.findByEmail(authentication.getName());
		}
		if (byEmail.isPresent()) {
			return byEmail;
		}

		return publicId(principal.subject())
				.or(() -> publicId(authentication.getName()))
				.flatMap(userRepository::findByPublicId);
	}

	private static Optional<UUID> publicId(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(value));
		} catch (IllegalArgumentException exception) {
			return Optional.empty();
		}
	}

	@Transactional
	public UserDto updateCurrentUser(UpdateUserProfileRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new UnauthorizedException();
		}

		User user = resolveUser(authentication)
				.orElseThrow(UnauthorizedException::new);

		if (request.firstName() != null) {
			user.setFirstName(request.firstName());
		}
		if (request.lastName() != null) {
			user.setLastName(request.lastName());
		}
		if (request.displayName() != null && !request.displayName().isBlank()) {
			user.setFullName(request.displayName());
		} else if (user.getFirstName() != null || user.getLastName() != null) {
			String combined = String.join(" ",
					user.getFirstName() != null ? user.getFirstName() : "",
					user.getLastName() != null ? user.getLastName() : "").trim();
			if (!combined.isEmpty()) {
				user.setFullName(combined);
			}
		}

		if (request.phone() != null) {
			user.setPhone(request.phone());
		}
		if (request.address() != null) {
			user.setAddress(request.address());
		}
		if (request.subdistrict() != null) {
			user.setSubdistrict(request.subdistrict());
		}
		if (request.district() != null) {
			user.setDistrict(request.district());
		}
		if (request.province() != null) {
			user.setProvince(request.province());
		}
		if (request.avatarUrl() != null) {
			user.setAvatarUrl(request.avatarUrl());
		}

		user.setUpdatedAt(Instant.now());
		User saved = userRepository.save(user);
		return userMapper.toDto(saved);
	}

}
