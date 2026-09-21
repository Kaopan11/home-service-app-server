package com.team.home_service_app_server.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.team.home_service_app_server.config.SupabaseProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.Jwks;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

	private static final long CLOCK_SKEW_SECONDS = 60;

	private final List<SecretKey> hmacKeys;
	private final RestClient restClient;
	private volatile Map<String, Key> jwksByKid = Map.of();

	public JwtService(SupabaseProperties properties) {
		this.hmacKeys = hmacKeysFromSecret(properties.jwtSecret());
		this.restClient = RestClient.builder()
				.baseUrl(properties.url())
				.build();
		refreshJwks();
	}

	public Claims parse(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}
		try {
			return Jwts.parser()
					.clockSkewSeconds(CLOCK_SKEW_SECONDS)
					.keyLocator(new LocatorAdapter<Key>() {
						@Override
						protected Key locate(JwsHeader header) {
							return locateVerificationKey(header);
						}
					})
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (JwtException | IllegalArgumentException exception) {
			return tryHmacKeys(token);
		}
	}

	public JwtPrincipal principal(Claims claims) {
		if (claims == null) {
			return new JwtPrincipal(null, null);
		}
		String email = stringClaim(claims, "email");
		if (email == null) {
			email = nestedEmail(claims.get("user_metadata"));
		}
		if (email == null) {
			email = stringClaim(claims, "phone");
		}
		return new JwtPrincipal(email, claims.getSubject());
	}

	private Claims tryHmacKeys(String token) {
		for (SecretKey key : hmacKeys) {
			try {
				return Jwts.parser()
						.clockSkewSeconds(CLOCK_SKEW_SECONDS)
						.verifyWith(key)
						.build()
						.parseSignedClaims(token)
						.getPayload();
			} catch (JwtException | IllegalArgumentException ignored) {
				// try next encoding of the dashboard secret
			}
		}
		return null;
	}

	private Key locateVerificationKey(JwsHeader header) {
		String algorithm = header.getAlgorithm();
		if (algorithm != null && algorithm.startsWith("HS") && !hmacKeys.isEmpty()) {
			return hmacKeys.get(0);
		}
		String kid = header.getKeyId();
		if (kid != null) {
			Key cached = jwksByKid.get(kid);
			if (cached != null) {
				return cached;
			}
			refreshJwks();
			return jwksByKid.get(kid);
		}
		if (!hmacKeys.isEmpty()) {
			return hmacKeys.get(0);
		}
		return null;
	}

	private void refreshJwks() {
		try {
			String body = restClient.get()
					.uri("/auth/v1/.well-known/jwks.json")
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(String.class);
			if (body == null || body.isBlank()) {
				return;
			}
			Map<String, Key> next = new LinkedHashMap<>();
			for (Jwk<?> jwk : Jwks.setParser().build().parse(body)) {
				if (jwk.getId() != null && jwk.toKey() != null) {
					next.put(jwk.getId(), jwk.toKey());
				}
			}
			jwksByKid = Map.copyOf(next);
		} catch (Exception ignored) {
			// HS256 secret path still applies when JWKS is unavailable
		}
	}

	private static List<SecretKey> hmacKeysFromSecret(String secret) {
		List<SecretKey> keys = new ArrayList<>();
		if (secret == null || secret.isBlank()) {
			return keys;
		}
		addHmacKey(keys, secret.getBytes(StandardCharsets.UTF_8));
		decodeBase64(secret).ifPresent(bytes -> addHmacKey(keys, bytes));
		return List.copyOf(keys);
	}

	private static void addHmacKey(List<SecretKey> keys, byte[] bytes) {
		if (bytes.length < 32) {
			return;
		}
		SecretKey key = Keys.hmacShaKeyFor(bytes);
		if (keys.stream().noneMatch(existing -> Arrays.equals(existing.getEncoded(), key.getEncoded()))) {
			keys.add(key);
		}
	}

	private static Optional<byte[]> decodeBase64(String secret) {
		String trimmed = secret.trim();
		try {
			return Optional.of(Base64.getDecoder().decode(trimmed));
		} catch (IllegalArgumentException ignored) {
			try {
				return Optional.of(Base64.getUrlDecoder().decode(trimmed));
			} catch (IllegalArgumentException ignoredAgain) {
				return Optional.empty();
			}
		}
	}

	private static String stringClaim(Claims claims, String name) {
		Object value = claims.get(name);
		return value instanceof String text && !text.isBlank() ? text : null;
	}

	private static String nestedEmail(Object metadata) {
		if (!(metadata instanceof Map<?, ?> map)) {
			return null;
		}
		Object email = map.get("email");
		return email instanceof String text && !text.isBlank() ? text : null;
	}
}
