package com.team.home_service_app_server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.team.home_service_app_server.dto.category.CategoryApiResponse;

@RestControllerAdvice
public class CategoryExceptionHandler {

	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<CategoryApiResponse> handleNotFound(CategoryNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CategoryApiResponse.notFound());
	}

	@ExceptionHandler(CategoryValidationException.class)
	public ResponseEntity<CategoryApiResponse> handleValidation(CategoryValidationException exception) {
		return ResponseEntity.badRequest().body(CategoryApiResponse.validation(exception.getMessage()));
	}

	@ExceptionHandler(CategoryNameConflictException.class)
	public ResponseEntity<CategoryApiResponse> handleConflict(CategoryNameConflictException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(CategoryApiResponse.conflict(
				exception.getCode(),
				exception.getMessage(),
				new CategoryApiResponse.ConflictData(exception.getCategoryId(), exception.isActive())));
	}

}
