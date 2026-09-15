package com.team.home_service_app_server.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
		String message,
		String code,
		List<FieldError> errors
) {

	public record FieldError(String field, String message) {
	}

}
