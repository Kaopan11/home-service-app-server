package com.team.home_service_app_server.dto;

import java.util.List;

public record ServiceDetailDto(
		Long id,
		String name,
		String categoryName,
		String image,
		List<ServiceOptionDto> options
) {
}
