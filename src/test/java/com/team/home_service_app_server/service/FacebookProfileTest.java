package com.team.home_service_app_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.team.home_service_app_server.exception.InvalidCredentialsException;

class FacebookProfileTest {

	private static final String AUTH_ID = "11111111-1111-1111-1111-111111111111";

	@Test
	void readsEmailAndNameFromFacebookClaims() {
		FacebookProfile profile = FacebookProfile.from(Map.of(
				"sub", AUTH_ID,
				"email", "Ann@Example.com",
				"user_metadata", Map.of(
						"full_name", "Ann Bee",
						"avatar_url", "https://example.com/a.png")));

		assertEquals(UUID.fromString(AUTH_ID), profile.authId());
		assertEquals("ann@example.com", profile.email());
		assertEquals("Ann Bee", profile.fullName());
		assertEquals("https://example.com/a.png", profile.avatarUrl());
	}

	@Test
	void rejectsFacebookAccountWithoutEmail() {
		assertThrows(InvalidCredentialsException.class, () -> FacebookProfile.from(Map.of(
				"sub", AUTH_ID,
				"user_metadata", Map.of("name", "Ann"))));
	}

}
