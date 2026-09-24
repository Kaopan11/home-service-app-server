package com.team.home_service_app_server.dto;

public record AdminServiceResponse(String message, AdminServiceDto data) {

	public static AdminServiceResponse success(AdminServiceDto data) {
		return new AdminServiceResponse("Success", data);
	}
}
