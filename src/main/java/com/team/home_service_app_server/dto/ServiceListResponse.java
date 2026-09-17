package com.team.home_service_app_server.dto;

import java.util.List;

public record ServiceListResponse(String message, List<ServiceListItemDto> data) {

	public static ServiceListResponse success(List<ServiceListItemDto> data) {
		return new ServiceListResponse("Success", data);
	}
}
