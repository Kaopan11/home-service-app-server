package com.team.home_service_app_server.dto.technician;

public record TechnicianJobResponse(String message, TechnicianJobDto data) {

	public static TechnicianJobResponse success(TechnicianJobDto data) {
		return new TechnicianJobResponse("Success", data);
	}

}
