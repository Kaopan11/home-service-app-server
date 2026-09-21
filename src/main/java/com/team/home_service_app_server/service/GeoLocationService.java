package com.team.home_service_app_server.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class GeoLocationService {

	private static final Pattern DISPLAY_NAME = Pattern.compile(
			"\"display_name\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	public String reverseGeocode(double latitude, double longitude) {
		try {
			URI uri = URI.create(String.format(
					Locale.US,
					"https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=%f&lon=%f&accept-language=th",
					latitude,
					longitude));
			HttpRequest request = HttpRequest.newBuilder(uri)
					.header("User-Agent", "home-service-app/1.0")
					.timeout(Duration.ofSeconds(8))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				String displayName = extractDisplayName(response.body());
				if (displayName != null && !displayName.isBlank()) {
					return displayName;
				}
			}
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}
		return String.format(Locale.US, "%.5f, %.5f", latitude, longitude);
	}

	private static String extractDisplayName(String json) {
		Matcher matcher = DISPLAY_NAME.matcher(json);
		if (!matcher.find()) {
			return null;
		}
		return matcher.group(1)
				.replace("\\\"", "\"")
				.replace("\\\\", "\\");
	}
}
