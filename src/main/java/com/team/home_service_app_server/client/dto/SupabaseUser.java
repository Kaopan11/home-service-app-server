package com.team.home_service_app_server.client.dto;

import java.util.List;

public record SupabaseUser(
		String id,
		String email,
		List<Object> identities
) {
}
