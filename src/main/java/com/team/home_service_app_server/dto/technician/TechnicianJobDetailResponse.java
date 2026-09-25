package com.team.home_service_app_server.dto.technician;

public record TechnicianJobDetailResponse(String message, TechnicianJobDetailDto data) {

	public static TechnicianJobDetailResponse success(TechnicianJobDetailDto data) {
		return new TechnicianJobDetailResponse("Success", data);
	}

}
