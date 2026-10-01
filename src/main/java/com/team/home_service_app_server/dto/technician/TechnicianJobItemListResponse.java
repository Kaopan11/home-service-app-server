package com.team.home_service_app_server.dto.technician;

import java.util.List;

public record TechnicianJobItemListResponse(String message, List<TechnicianJobItemDto> data) {

	public static TechnicianJobItemListResponse success(List<TechnicianJobItemDto> data) {
		return new TechnicianJobItemListResponse("Success", data);
	}

}
