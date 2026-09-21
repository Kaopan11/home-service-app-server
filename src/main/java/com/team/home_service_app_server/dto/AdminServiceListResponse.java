package com.team.home_service_app_server.dto;

import java.util.List;

public record AdminServiceListResponse(String message, List<AdminServiceDto> data) {

	public static AdminServiceListResponse success(List<AdminServiceDto> data) {
		return new AdminServiceListResponse("Success", data);
	}
}
