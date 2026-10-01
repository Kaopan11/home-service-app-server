package com.team.home_service_app_server.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.client.SupabaseAuthClient;
import com.team.home_service_app_server.dto.ApiError;
import com.team.home_service_app_server.dto.ChangePasswordRequest;
import com.team.home_service_app_server.dto.ChangePasswordResponse;
import com.team.home_service_app_server.dto.UpdateUserProfileRequest;
import com.team.home_service_app_server.dto.UserDto;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.entity.UserRole;
import com.team.home_service_app_server.exception.ForbiddenException;
import com.team.home_service_app_server.exception.InvalidCredentialsException;
import com.team.home_service_app_server.exception.PasswordChangeException;
import com.team.home_service_app_server.exception.UnauthorizedException;
import com.team.home_service_app_server.mapper.UserMapper;
import com.team.home_service_app_server.repository.UserRepository;
import com.team.home_service_app_server.security.JwtPrincipal;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final SupabaseAuthClient supabaseAuthClient;

	public UserService(UserRepository userRepository, UserMapper userMapper, SupabaseAuthClient supabaseAuthClient) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.supabaseAuthClient = supabaseAuthClient;
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

	public ChangePasswordResponse changePassword(String accessToken, ChangePasswordRequest request) {
		ChangePasswordRequest body = request == null
				? new ChangePasswordRequest(null, null, null, null)
				: request;
		List<ApiError.FieldError> errors = passwordErrors(body);
		if (!errors.isEmpty()) {
			throw new PasswordChangeException("VALIDATION_ERROR", "ข้อมูลรหัสผ่านไม่ถูกต้อง", errors);
		}

		User user = requireCurrentUserEntity();
		if (accessToken == null || user.getEmail() == null || user.getEmail().isBlank()) {
			throw new UnauthorizedException();
		}

		try {
			supabaseAuthClient.signInWithPassword(user.getEmail(), body.currentPassword());
		} catch (InvalidCredentialsException exception) {
			throw new PasswordChangeException(
					"CURRENT_PASSWORD_INVALID",
					"ข้อมูลรหัสผ่านไม่ถูกต้อง",
					List.of(new ApiError.FieldError("currentPassword", "รหัสผ่านปัจจุบันไม่ถูกต้อง")));
		}

		try {
			supabaseAuthClient.updatePassword(accessToken, body.newPassword());
		} catch (IllegalStateException exception) {
			throw new PasswordChangeException("PASSWORD_UPDATE_FAILED", "ไม่สามารถเปลี่ยนรหัสผ่านได้", null);
		}

		return ChangePasswordResponse.success();
	}

	private static List<ApiError.FieldError> passwordErrors(ChangePasswordRequest body) {
		List<ApiError.FieldError> errors = new ArrayList<>();
		String current = body.currentPassword();
		String next = body.newPassword();
		String confirm = body.confirm();
		if (current == null || current.isEmpty()) {
			errors.add(new ApiError.FieldError("currentPassword", "กรุณากรอกรหัสผ่านปัจจุบัน"));
		}
		if (next == null || next.isEmpty()) {
			errors.add(new ApiError.FieldError("newPassword", "กรุณากรอกรหัสผ่านใหม่"));
		} else if (next.length() < 6) {
			errors.add(new ApiError.FieldError("newPassword", "รหัสผ่านใหม่ต้องมีความยาวอย่างน้อย 6 ตัวอักษร"));
		}
		if (confirm == null || confirm.isEmpty()) {
			errors.add(new ApiError.FieldError("confirmNewPassword", "กรุณายืนยันรหัสผ่านใหม่"));
		} else if (next != null && !confirm.equals(next)) {
			errors.add(new ApiError.FieldError("confirmNewPassword", "รหัสผ่านใหม่และยืนยันรหัสผ่านไม่ตรงกัน"));
		}
		if (current != null && next != null && !current.isEmpty() && current.equals(next)) {
			errors.add(new ApiError.FieldError("newPassword", "รหัสผ่านใหม่ต้องไม่ซ้ำกับรหัสผ่านปัจจุบัน"));
		}
		return errors;
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
