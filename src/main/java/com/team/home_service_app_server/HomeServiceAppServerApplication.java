package com.team.home_service_app_server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HomeServiceAppServerApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(HomeServiceAppServerApplication.class, args);
	}

	// ponytail: local .env only; no file = same as before, Render still uses OS env
	private static void loadDotEnv() {
		Path file = Path.of(".env");
		if (!Files.isRegularFile(file)) {
			return;
		}
		try {
			for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
				String line = raw.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int eq = line.indexOf('=');
				if (eq <= 0) {
					continue;
				}
				String key = line.substring(0, eq).trim();
				if (System.getenv(key) != null) {
					continue;
				}
				System.setProperty(key, stripQuotes(line.substring(eq + 1).trim()));
			}
		} catch (IOException ignored) {
			// unreadable .env is fine
		}
	}

	private static String stripQuotes(String value) {
		if (value.length() >= 2) {
			char first = value.charAt(0);
			char last = value.charAt(value.length() - 1);
			if (first == last && (first == '"' || first == '\'')) {
				return value.substring(1, value.length() - 1);
			}
		}
		return value;
	}

}
