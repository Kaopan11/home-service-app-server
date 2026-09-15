package com.team.home_service_app_server.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.team.home_service_app_server.config.SupabaseProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private final SecretKey secretKey;

	public JwtService(SupabaseProperties properties) {
		this.secretKey = Keys.hmacShaKeyFor(padSecret(properties.jwtSecret()));
	}

	public Claims parse(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (JwtException | IllegalArgumentException exception) {
			return null;
		}
	}

	private byte[] padSecret(String secret) {
		byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length >= 32) {
			return bytes;
		}
		byte[] padded = new byte[32];
		System.arraycopy(bytes, 0, padded, 0, bytes.length);
		return padded;
	}

}
