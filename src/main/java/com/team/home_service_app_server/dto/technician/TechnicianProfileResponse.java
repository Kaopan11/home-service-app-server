package com.team.home_service_app_server.dto.technician;

public record TechnicianProfileResponse(String message, TechnicianProfileDto data) {

	public static TechnicianProfileResponse success(TechnicianProfileDto data) {
		return new TechnicianProfileResponse("Success", data);
	}

}
