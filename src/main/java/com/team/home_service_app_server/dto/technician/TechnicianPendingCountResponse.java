package com.team.home_service_app_server.dto.technician;

public record TechnicianPendingCountResponse(String message, TechnicianPendingCountDto data) {

	public static TechnicianPendingCountResponse success(long count) {
		return new TechnicianPendingCountResponse("Success", new TechnicianPendingCountDto(count));
	}

}
