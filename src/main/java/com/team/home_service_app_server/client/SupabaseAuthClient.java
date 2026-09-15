package com.team.home_service_app_server.client;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.team.home_service_app_server.client.dto.SupabaseTokenResponse;
import com.team.home_service_app_server.config.SupabaseProperties;
import com.team.home_service_app_server.exception.InvalidCredentialsException;

@Component
public class SupabaseAuthClient {

	private final RestClient restClient;
	private final SupabaseProperties properties;

	public SupabaseAuthClient(SupabaseProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder()
				.baseUrl(properties.url())
				.build();
	}

	public SupabaseTokenResponse signInWithPassword(String email, String password) {
		try {
			return restClient.post()
					.uri("/auth/v1/token?grant_type=password")
					.header("apikey", properties.anonKey())
					.header("Authorization", "Bearer " + properties.anonKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("email", email, "password", password))
					.retrieve()
					.body(SupabaseTokenResponse.class);
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				throw new InvalidCredentialsException();
			}
			throw exception;
		}
	}

	public void signOut(String accessToken) {
		if (accessToken == null || accessToken.isBlank()) {
			return;
		}
		try {
			restClient.post()
					.uri("/auth/v1/logout")
					.header("apikey", properties.anonKey())
					.header("Authorization", "Bearer " + accessToken)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is4xxClientError()) {
				return;
			}
			throw exception;
		}
	}

}
