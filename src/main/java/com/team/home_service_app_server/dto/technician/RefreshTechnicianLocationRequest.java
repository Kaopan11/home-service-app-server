package com.team.home_service_app_server.dto.technician;

import jakarta.validation.constraints.NotNull;

public record RefreshTechnicianLocationRequest(
		@NotNull(message = "ไม่พบละติจูด") Double latitude,
		@NotNull(message = "ไม่พบลองจิจูด") Double longitude
) {
}
