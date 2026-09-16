package com.team.home_service_app_server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(
		String url,
		String anonKey,
		String jwtSecret,
		String serviceRoleKey
) {
}
