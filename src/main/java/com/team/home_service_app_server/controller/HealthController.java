package com.team.home_service_app_server.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "ok");
	}

	@GetMapping("/")
	public Map<String, String> root() {
		return Map.of("message", "API is working");
	}

}
