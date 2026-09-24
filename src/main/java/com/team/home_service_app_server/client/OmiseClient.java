package com.team.home_service_app_server.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.team.home_service_app_server.client.dto.OmiseChargeResponse;
import com.team.home_service_app_server.client.dto.OmiseErrorResponse;
import com.team.home_service_app_server.config.OmiseProperties;
import com.team.home_service_app_server.exception.PaymentFailedException;

@Component
public class OmiseClient {

	private final RestClient restClient;

	public OmiseClient(OmiseProperties properties) {
		this.restClient = RestClient.builder()
				.baseUrl("https://api.omise.co")
				.requestInterceptor((request, body, execution) -> {
					request.getHeaders().setBasicAuth(properties.secretKey(), "");
					return execution.execute(request, body);
				})
				.build();
	}

	public OmiseChargeResponse createCharge(String token, long amountSatang, String currency, String description) {
		if (token == null || token.isBlank()) {
			throw new PaymentFailedException("Token การชำระเงินไม่ถูกต้อง");
		}
		if (amountSatang <= 0) {
			throw new PaymentFailedException("จำนวนเงินต้องมากกว่า 0");
		}

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("amount", amountSatang);
		body.put("currency", currency);
		body.put("card", token);
		if (description != null && !description.isBlank()) {
			body.put("description", description);
		}

		try {
			OmiseChargeResponse response = restClient.post()
					.uri("/charges")
					.contentType(MediaType.APPLICATION_JSON)
					// Derived from the (one-time-use) token so a duplicate request carrying
					// the same token - e.g. a double click or a network-layer retry - reuses
					// the original charge instead of billing the card twice.
					.header("Idempotency-Key", idempotencyKeyFor(token))
					.body(body)
					.retrieve()
					.body(OmiseChargeResponse.class);
			if (response == null) {
				throw new PaymentFailedException("ไม่ได้รับผลลัพธ์จาก Omise");
			}
			return response;
		} catch (RestClientResponseException exception) {
			throw new PaymentFailedException(extractErrorMessage(exception));
		}
	}

	private String idempotencyKeyFor(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			return "charge-" + HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException exception) {
			// SHA-256 is always available on the standard JVM providers; this is unreachable.
			throw new IllegalStateException(exception);
		}
	}

	private String extractErrorMessage(RestClientResponseException exception) {
		try {
			OmiseErrorResponse error = exception.getResponseBodyAs(OmiseErrorResponse.class);
			if (error != null && error.message() != null && !error.message().isBlank()) {
				return error.message();
			}
		} catch (Exception ignored) {
			// fall back to generic message below
		}
		return "การชำระเงินไม่สำเร็จ กรุณาตรวจสอบข้อมูลบัตรอีกครั้ง";
	}

}
