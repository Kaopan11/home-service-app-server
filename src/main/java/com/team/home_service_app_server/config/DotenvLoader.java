package com.team.home_service_app_server.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DotenvLoader {

	private DotenvLoader() {
	}

	public static Path envFile() {
		return Path.of(System.getProperty("user.dir", ".")).resolve(".env");
	}

	public static Map<String, Object> read() {
		Path envFile = envFile();
		if (!Files.isRegularFile(envFile)) {
			return Map.of();
		}

		Map<String, Object> values = new LinkedHashMap<>();
		try {
			for (String rawLine : Files.readAllLines(envFile, StandardCharsets.UTF_8)) {
				String line = rawLine.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int separator = line.indexOf('=');
				if (separator <= 0) {
					continue;
				}
				String key = line.substring(0, separator).trim();
				String value = stripQuotes(line.substring(separator + 1).trim());
				values.put(key, value);
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to read .env from " + envFile.toAbsolutePath(), exception);
		}
		return values;
	}

	public static void copyIntoSystemProperties(Map<String, Object> values) {
		values.forEach((key, value) -> {
			if (System.getenv(key) == null && System.getProperty(key) == null) {
				System.setProperty(key, String.valueOf(value));
			}
		});
	}

	private static String stripQuotes(String value) {
		if (value.length() >= 2) {
			char first = value.charAt(0);
			char last = value.charAt(value.length() - 1);
			if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
				return value.substring(1, value.length() - 1);
			}
		}
		return value;
	}

}
