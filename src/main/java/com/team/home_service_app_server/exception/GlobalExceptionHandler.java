package com.team.home_service_app_server.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.team.home_service_app_server.dto.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
		List<ApiError.FieldError> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new ApiError.FieldError(
						fieldError.getField(),
						fieldError.getDefaultMessage()))
				.toList();

		return ResponseEntity.badRequest().body(new ApiError(
				"ข้อมูลไม่ถูกต้อง",
				"VALIDATION_ERROR",
				errors));
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
				exception.getMessage(),
				"CONFLICT",
				null));
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
				exception.getMessage(),
				"INVALID_CREDENTIALS",
				null));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
				"ไม่มีสิทธิ์เข้าถึง",
				"UNAUTHORIZED",
				null));
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> handleForbidden(ForbiddenException exception) {
		String message = exception.getMessage() == null || exception.getMessage().isBlank()
				? "บัญชีนี้ไม่มีสิทธิ์เข้าถึงระบบ Admin"
				: exception.getMessage();
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(
				message,
				"FORBIDDEN_ROLE",
				null));
	}

	@ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
	public ResponseEntity<ApiError> handleResponseStatus(
			org.springframework.web.server.ResponseStatusException exception) {
		String message = exception.getReason() == null || exception.getReason().isBlank()
				? exception.getMessage()
				: exception.getReason();
		return ResponseEntity.status(exception.getStatusCode()).body(new ApiError(
				message,
				"NOT_FOUND",
				null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneral(Exception exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(
				exception.getMessage() != null && !exception.getMessage().isBlank() ? exception.getMessage() : "เกิดข้อผิดพลาดภายในระบบ",
				"INTERNAL_SERVER_ERROR",
				null));
	}

}
