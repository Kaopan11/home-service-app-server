package com.team.home_service_app_server.security;

public record JwtPrincipal(String email, String subject) {

	public String lookupKey() {
		if (email != null && !email.isBlank()) {
			return email;
		}
		return subject;
	}

	public boolean isResolved() {
		return lookupKey() != null && !lookupKey().isBlank();
	}
}
