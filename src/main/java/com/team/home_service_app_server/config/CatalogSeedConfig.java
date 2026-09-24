package com.team.home_service_app_server.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CatalogSeedConfig {

	@Bean
	@Order(0)
	CommandLineRunner widenServiceImageUrl(JdbcTemplate jdbc) {
		return args -> {
			try {
				jdbc.execute("ALTER TABLE services ALTER COLUMN image_url TYPE TEXT");
				jdbc.execute("ALTER TABLE service_options ADD COLUMN IF NOT EXISTS display_order INTEGER NOT NULL DEFAULT 0");
				jdbc.execute("ALTER TABLE categories ADD COLUMN IF NOT EXISTS display_order INTEGER NOT NULL DEFAULT 0");
			} catch (Exception ignored) {
				// ponytail: schema tweak is best-effort; PgBouncer can fail a prepared ALTER without blocking boot
			}
		};
	}
}
