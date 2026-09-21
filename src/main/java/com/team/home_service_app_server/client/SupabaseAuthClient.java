package com.team.home_service_app_server.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.team.home_service_app_server.client.dto.SupabaseAdminUser;
import com.team.home_service_app_server.client.dto.SupabaseTokenResponse;
import com.team.home_service_app_server.config.SupabaseProperties;
import com.team.home_service_app_server.exception.ConflictException;
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
				throw new InvalidCredentialsException("รหัสผ่านไม่ถูกต้อง");
			}
			throw exception;
		}
	}

	public UUID createUser(String email, String password, String fullName, String phone) {
		try {
			SupabaseAdminUser created = restClient.post()
					.uri("/auth/v1/admin/users")
					.header("apikey", properties.serviceRoleKey())
					.header("Authorization", "Bearer " + properties.serviceRoleKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"email", email,
							"password", password,
							"email_confirm", true,
							"user_metadata", Map.of("full_name", fullName, "phone", phone)))
					.retrieve()
					.body(SupabaseAdminUser.class);
			if (created == null || created.id() == null) {
				throw new IllegalStateException("สร้างบัญชี Supabase ไม่สำเร็จ");
			}
			return UUID.fromString(created.id());
		} catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == 422 || exception.getStatusCode().value() == 409) {
				throw new ConflictException("อีเมลนี้ถูกใช้แล้ว");
			}
			throw new IllegalStateException("ไม่สามารถสร้างบัญชีผู้ใช้ใน Supabase ได้ (" + exception.getStatusCode() + "): " + exception.getResponseBodyAsString());
		}
	}

	public void deleteUser(UUID userId) {
		try {
			restClient.delete()
					.uri("/auth/v1/admin/users/{id}", userId)
					.header("apikey", properties.serviceRoleKey())
					.header("Authorization", "Bearer " + properties.serviceRoleKey())
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ignored) {
			// rollback best-effort
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
