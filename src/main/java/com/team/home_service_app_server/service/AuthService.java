package com.team.home_service_app_server.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.team.home_service_app_server.client.SupabaseAuthClient;
import com.team.home_service_app_server.client.dto.SupabaseTokenResponse;
import com.team.home_service_app_server.dto.LoginRequest;
import com.team.home_service_app_server.dto.LoginResponse;
import com.team.home_service_app_server.dto.SessionDto;
import com.team.home_service_app_server.dto.UserDto;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.InvalidCredentialsException;
import com.team.home_service_app_server.repository.UserRepository;

@Service
public class AuthService {

	private final SupabaseAuthClient supabaseAuthClient;
	private final UserRepository userRepository;

	public AuthService(SupabaseAuthClient supabaseAuthClient, UserRepository userRepository) {
		this.supabaseAuthClient = supabaseAuthClient;
		this.userRepository = userRepository;
	}

	public LoginResponse login(LoginRequest request) {
		SupabaseTokenResponse tokenResponse = supabaseAuthClient.signInWithPassword(
				request.email(),
				request.password());

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		return LoginResponse.success(toUserDto(user), toSessionDto(tokenResponse));
	}

	private UserDto toUserDto(User user) {
		String displayName = resolveDisplayName(user);
		return new UserDto(
				user.getUserId(),
				user.getEmail(),
				user.getFullName(),
				displayName,
				user.getFirstName(),
				user.getLastName(),
				user.getPhone(),
				null,
				user.getAvatarUrl(),
				user.getRole().name());
	}

	private String resolveDisplayName(User user) {
		if (user.getFullName() != null && !user.getFullName().isBlank()) {
			return user.getFullName();
		}
		if (user.getFirstName() != null || user.getLastName() != null) {
			return String.join(" ",
					user.getFirstName() != null ? user.getFirstName() : "",
					user.getLastName() != null ? user.getLastName() : "").trim();
		}
		return user.getEmail();
	}

	private SessionDto toSessionDto(SupabaseTokenResponse tokenResponse) {
		long expiresAt = Instant.now().getEpochSecond() + tokenResponse.expiresIn();
		return new SessionDto(
				tokenResponse.accessToken(),
				tokenResponse.refreshToken(),
				expiresAt,
				tokenResponse.tokenType());
	}

}
