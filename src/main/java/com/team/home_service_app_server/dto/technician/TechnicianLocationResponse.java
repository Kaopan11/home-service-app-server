package com.team.home_service_app_server.dto.technician;

public record TechnicianLocationResponse(String message, TechnicianLocationDto data) {

	public static TechnicianLocationResponse success(TechnicianLocationDto data) {
		return new TechnicianLocationResponse("Success", data);
	}

}
