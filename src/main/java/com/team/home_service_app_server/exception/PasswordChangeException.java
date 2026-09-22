package com.team.home_service_app_server.exception;

import java.util.List;

import com.team.home_service_app_server.dto.ApiError;

public class PasswordChangeException extends RuntimeException {

	private final String code;
	private final List<ApiError.FieldError> errors;

	public PasswordChangeException(String code, String message, List<ApiError.FieldError> errors) {
		super(message);
		this.code = code;
		this.errors = errors;
	}

	public String code() {
		return code;
	}

	public List<ApiError.FieldError> errors() {
		return errors;
	}

}
