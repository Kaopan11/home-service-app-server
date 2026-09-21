package com.team.home_service_app_server.dto.technician;

import java.util.List;

public record TechnicianJobListResponse(String message, List<TechnicianJobDto> data) {

	public static TechnicianJobListResponse success(List<TechnicianJobDto> data) {
		return new TechnicianJobListResponse("Success", data);
	}

}
