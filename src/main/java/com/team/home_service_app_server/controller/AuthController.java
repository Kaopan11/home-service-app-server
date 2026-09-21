package com.team.home_service_app_server.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.FacebookLoginRequest;
import com.team.home_service_app_server.dto.LoginRequest;
import com.team.home_service_app_server.dto.LoginResponse;
import com.team.home_service_app_server.dto.MessageResponse;
import com.team.home_service_app_server.dto.RegisterRequest;
import com.team.home_service_app_server.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String BEARER_PREFIX = "Bearer ";

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/register")
	public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	/** กดปุ่ม Facebook แล้วเบราว์เซอร์มาที่นี่ จากนั้นถูกพาไปหน้า Facebook */
	@GetMapping("/facebook")
	public void startFacebook(
			@RequestParam(required = false) String redirectTo,
			HttpServletResponse response) throws IOException {
		response.sendRedirect(authService.facebookAuthorizeUrl(redirectTo));
	}

	/** Facebook กลับมาแล้ว ส่ง token มาที่นี่เพื่อเข้าสู่ระบบ */
	@PostMapping("/facebook")
	public LoginResponse loginFacebook(@Valid @RequestBody FacebookLoginRequest request) {
		return authService.loginWithFacebook(request);
	}

	@PostMapping("/logout")
	public MessageResponse logout(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
		authService.logout(extractBearer(authorization));
		return MessageResponse.logoutSuccess();
	}

	private String extractBearer(String authorization) {
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length()).trim();
	}

}
