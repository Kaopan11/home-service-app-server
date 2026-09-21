package com.team.home_service_app_server.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.team.home_service_app_server.client.SupabaseAuthClient;
import com.team.home_service_app_server.client.dto.SupabaseTokenResponse;
import com.team.home_service_app_server.config.SupabaseProperties;
import com.team.home_service_app_server.dto.FacebookLoginRequest;
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
import com.team.home_service_app_server.security.JwtService;

import io.jsonwebtoken.Claims;

@Service
public class AuthService {

	private static final String DEFAULT_CALLBACK = "http://localhost:5173/auth/callback";
	private static final String DEFAULT_ORIGINS =
			"http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:3000";

	private final SupabaseAuthClient supabaseAuthClient;
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final JwtService jwtService;
	private final SupabaseProperties supabaseProperties;
	private final List<String> allowedOrigins;

	public AuthService(
			SupabaseAuthClient supabaseAuthClient,
			UserRepository userRepository,
			UserMapper userMapper,
			JwtService jwtService,
			SupabaseProperties supabaseProperties,
			@Value("${CORS_ALLOWED_ORIGINS:" + DEFAULT_ORIGINS + "}") String allowedOrigins) {
		this.supabaseAuthClient = supabaseAuthClient;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.jwtService = jwtService;
		this.supabaseProperties = supabaseProperties;
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList();
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

	/** สร้างลิงก์ไปหน้า Facebook ของ Supabase แล้วให้กลับมาที่เว็บเรา */
	public String facebookAuthorizeUrl(String redirectTo) {
		String callback = allowedCallback(redirectTo);
		String baseUrl = supabaseProperties.url().replaceAll("/$", "");
		return baseUrl
				+ "/auth/v1/authorize?provider=facebook"
				+ "&redirect_to=" + URLEncoder.encode(callback, StandardCharsets.UTF_8)
				+ "&apikey=" + URLEncoder.encode(supabaseProperties.anonKey(), StandardCharsets.UTF_8);
	}

	/** ตรวจ JWT จาก Facebook แล้วหา/สร้างแถวใน public.users */
	public LoginResponse loginWithFacebook(FacebookLoginRequest request) {
		Claims claims = jwtService.parse(request.accessToken());
		FacebookProfile profile = FacebookProfile.from(claims);
		User user = userRepository.findByPublicId(profile.authId())
				.or(() -> userRepository.findByEmail(profile.email()))
				.orElseGet(() -> createFacebookUser(profile));

		String refreshToken = request.refreshToken() == null ? "" : request.refreshToken();
		return LoginResponse.success(userMapper.toDto(user), new SessionDto(
				request.accessToken(),
				refreshToken,
				expiresAt(claims, request.expiresIn()),
				"bearer"));
	}

	private User createFacebookUser(FacebookProfile profile) {
		User user = new User();
		Instant now = Instant.now();
		String[] names = profile.fullName().split("\\s+", 2);
		user.setPublicId(profile.authId());
		user.setUsername(profile.email());
		user.setFullName(profile.fullName());
		user.setFirstName(names[0]);
		user.setLastName(names.length > 1 ? names[1] : null);
		user.setEmail(profile.email());
		user.setAvatarUrl(profile.avatarUrl());
		user.setRole(UserRole.USER);
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		return userRepository.save(user);
	}

	private String allowedCallback(String redirectTo) {
		if (redirectTo != null) {
			for (String origin : allowedOrigins) {
				if (redirectTo.equals(origin + "/auth/callback")) {
					return redirectTo;
				}
			}
		}
		return DEFAULT_CALLBACK;
	}

	private static long expiresAt(Claims claims, Long expiresIn) {
		if (expiresIn != null && expiresIn > 0) {
			return Instant.now().getEpochSecond() + expiresIn;
		}
		if (claims.getExpiration() != null) {
			return claims.getExpiration().toInstant().getEpochSecond();
		}
		return Instant.now().getEpochSecond() + 3600;
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
