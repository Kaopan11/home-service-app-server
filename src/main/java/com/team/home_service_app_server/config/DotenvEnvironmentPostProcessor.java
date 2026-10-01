package com.team.home_service_app_server.config;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> values = DotenvLoader.read();
		if (values.isEmpty()) {
			return;
		}

		Map<String, Object> expanded = new LinkedHashMap<>(values);
		values.forEach((key, value) -> expanded.putIfAbsent(canonicalKey(key), value));
		environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, expanded));
	}

	private static String canonicalKey(String key) {
		return key.toLowerCase(Locale.ROOT).replace('_', '.');
	}
}
