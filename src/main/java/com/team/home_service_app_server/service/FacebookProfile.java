package com.team.home_service_app_server.service;

import java.util.Map;
import java.util.UUID;

import com.team.home_service_app_server.exception.InvalidCredentialsException;

/**
 * ดึงข้อมูลที่ใช้สร้างบัญชี จาก JWT ที่ Supabase ออกให้หลังล็อกอิน Facebook
 */
public record FacebookProfile(UUID authId, String email, String fullName, String avatarUrl) {

	public static FacebookProfile from(Map<String, Object> claims) {
		if (claims == null) {
			throw new InvalidCredentialsException("เข้าสู่ระบบด้วย Facebook ไม่สำเร็จ");
		}

		UUID authId;
		try {
			authId = UUID.fromString(text(claims.get("sub")));
		} catch (IllegalArgumentException | NullPointerException exception) {
			throw new InvalidCredentialsException("เข้าสู่ระบบด้วย Facebook ไม่สำเร็จ");
		}

		Map<String, Object> metadata = nestedMap(claims.get("user_metadata"));
		String email = firstText(claims.get("email"), metadata.get("email"));
		if (email == null) {
			throw new InvalidCredentialsException("บัญชี Facebook นี้ไม่มีอีเมล");
		}

		String fullName = firstText(
				metadata.get("full_name"),
				metadata.get("name"),
				claims.get("name"),
				email);
		String avatarUrl = firstText(metadata.get("avatar_url"), metadata.get("picture"));
		return new FacebookProfile(authId, email.toLowerCase(), fullName, avatarUrl);
	}

	private static Map<String, Object> nestedMap(Object value) {
		if (value instanceof Map<?, ?> map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> typed = (Map<String, Object>) map;
			return typed;
		}
		return Map.of();
	}

	private static String firstText(Object... values) {
		for (Object value : values) {
			String text = text(value);
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	private static String text(Object value) {
		if (!(value instanceof String text) || text.isBlank()) {
			return null;
		}
		return text.trim();
	}

}
