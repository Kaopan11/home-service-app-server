package com.team.home_service_app_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "omise")
public record OmiseProperties(
		String publicKey,
		String secretKey
) {
}
