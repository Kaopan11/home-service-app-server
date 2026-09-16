package com.team.home_service_app_server.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.team.home_service_app_server.client.SupabaseAuthClient;
import com.team.home_service_app_server.client.dto.SupabaseTokenResponse;
import com.team.home_service_app_server.dto.LoginRequest;
import com.team.home_service_app_server.dto.LoginResponse;
import com.team.home_service_app_server.dto.RegisterRequest;
import com.team.home_service_app_server.dto.SessionDto;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.entity.UserRole;
import com.team.home_service_app_server.exception.ConflictException;
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
		String email = request.email().trim().toLowerCase();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new InvalidCredentialsException("อีเมลไม่ถูกต้อง"));

		SupabaseTokenResponse tokenResponse = supabaseAuthClient.signInWithPassword(
				email,
				request.password());

		return LoginResponse.success(userMapper.toDto(user), toSessionDto(tokenResponse));
	}

	public LoginResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		String phone = request.phone().trim();
		String fullName = request.fullName().trim();

		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("อีเมลนี้ถูกใช้แล้ว");
		}
		if (userRepository.existsByPhone(phone)) {
			throw new ConflictException("เบอร์โทรศัพท์นี้ถูกใช้แล้ว");
		}

		// 1) สร้างบัญชีใน Supabase Auth (ยืนยันอีเมลให้เลย แล้วค่อย login ได้ทันที)
		UUID authUserId = supabaseAuthClient.createUser(email, request.password(), fullName, phone);

		User user;
		try {
			// 2) สร้างแถวใน public.users — id ต้องตรงกับ auth.users
			user = new User();
			Instant now = Instant.now();
			String[] names = fullName.split("\\s+", 2);
			user.setPublicId(authUserId);
			user.setUsername(email);
			user.setFullName(fullName);
			user.setFirstName(names[0]);
			user.setLastName(names.length > 1 ? names[1] : null);
			user.setPhone(phone);
			user.setEmail(email);
			user.setRole(UserRole.USER);
			user.setCreatedAt(now);
			user.setUpdatedAt(now);
			userRepository.save(user);
		} catch (RuntimeException exception) {
			supabaseAuthClient.deleteUser(authUserId);
			throw exception;
		}

		// 3) ล็อกอินทันทีเพื่อได้ token ส่งกลับ frontend
		SupabaseTokenResponse tokenResponse = supabaseAuthClient.signInWithPassword(
				email,
				request.password());
		return LoginResponse.registered(userMapper.toDto(user), toSessionDto(tokenResponse));
	}

	public void logout(String accessToken) {
		supabaseAuthClient.signOut(accessToken);
	}

	private SessionDto toSessionDto(SupabaseTokenResponse tokenResponse) {
		long expiresIn = tokenResponse.expiresIn() == null ? 3600 : tokenResponse.expiresIn();
		String tokenType = tokenResponse.tokenType() == null ? "bearer" : tokenResponse.tokenType();
		String refreshToken = tokenResponse.refreshToken() == null ? "" : tokenResponse.refreshToken();
		long expiresAt = Instant.now().getEpochSecond() + expiresIn;
		return new SessionDto(
				tokenResponse.accessToken(),
				refreshToken,
				expiresAt,
				tokenType);
	}

}
