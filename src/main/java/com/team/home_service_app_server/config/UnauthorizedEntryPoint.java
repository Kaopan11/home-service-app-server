package com.team.home_service_app_server.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

	private static final byte[] BODY = """
			{"message":"ไม่มีสิทธิ์เข้าถึง","code":"UNAUTHORIZED"}
			""".getBytes(StandardCharsets.UTF_8);

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getOutputStream().write(BODY);
	}

}
