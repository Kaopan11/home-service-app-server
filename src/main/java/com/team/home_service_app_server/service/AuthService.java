package com.team.home_service_app_server.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.team.home_service_app_server.client.SupabaseAuthClient;
import com.team.home_service_app_server.client.dto.SupabaseTokenResponse;
import com.team.home_service_app_server.dto.LoginRequest;
import com.team.home_service_app_server.dto.LoginResponse;
import com.team.home_service_app_server.dto.SessionDto;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.InvalidCredentialsException;
import com.team.home_service_app_server.mapper.UserMapper;
import com.team.home_service_app_server.repository.UserRepository;

@Service
public class AuthService {

	private final SupabaseAuthClient supabaseAuthClient;
	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public AuthService(
			SupabaseAuthClient supabaseAuthClient,
			UserRepository userRepository,
			UserMapper userMapper) {
		this.supabaseAuthClient = supabaseAuthClient;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public LoginResponse login(LoginRequest request) {
		SupabaseTokenResponse tokenResponse = supabaseAuthClient.signInWithPassword(
				request.email(),
				request.password());

		User user = userRepository.findByEmail(request.email())
				.orElseThrow(InvalidCredentialsException::new);

		return LoginResponse.success(userMapper.toDto(user), toSessionDto(tokenResponse));
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
