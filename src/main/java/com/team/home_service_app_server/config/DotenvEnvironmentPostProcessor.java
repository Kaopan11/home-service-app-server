package com.team.home_service_app_server.config;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final String PROPERTY_SOURCE_NAME = "dotenvFile";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Map<String, Object> values = DotenvLoader.read();
		if (values.isEmpty()) {
			return;
		}

		MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, values);
		if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
			environment.getPropertySources().addAfter(
					StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
					propertySource);
		} else {
			environment.getPropertySources().addFirst(propertySource);
		}
	}

}
