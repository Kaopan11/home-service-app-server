package com.team.home_service_app_server.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private static final String DEFAULT_ALLOWED_ORIGINS =
			"http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:3000";

	@Value("${CORS_ALLOWED_ORIGINS:" + DEFAULT_ALLOWED_ORIGINS + "}")
	private String allowedOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] origins = Arrays.stream(allowedOrigins.split(","))
				.map(origin -> origin.trim())
				.filter(origin -> !origin.isEmpty())
				.toArray(String[]::new);

		registry.addMapping("/**")
				.allowedOrigins(origins)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("Content-Type", "Authorization")
				.allowCredentials(true);
	}

}
