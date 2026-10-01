package com.team.home_service_app_server.dto;

public record ServiceDetailResponse(String message, ServiceDetailDto data) {

	public static ServiceDetailResponse success(ServiceDetailDto data) {
		return new ServiceDetailResponse("Success", data);
	}
}
