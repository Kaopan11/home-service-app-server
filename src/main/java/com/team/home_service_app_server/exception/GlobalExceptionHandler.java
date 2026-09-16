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

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
				"อีเมลหรือรหัสผ่านไม่ถูกต้อง",
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
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(
				"บัญชีนี้ไม่มีสิทธิ์เข้าถึงระบบ Admin",
				"FORBIDDEN_ROLE",
				null));
	}

}
