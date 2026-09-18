package com.team.home_service_app_server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.team.home_service_app_server.dto.promo.PromotionApiResponse;

@RestControllerAdvice
public class PromotionExceptionHandler {

	@ExceptionHandler(PromotionNotFoundException.class)
	public ResponseEntity<PromotionApiResponse> handleNotFound(PromotionNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(PromotionApiResponse.notFound());
	}

	@ExceptionHandler(PromotionValidationException.class)
	public ResponseEntity<PromotionApiResponse> handleValidation(PromotionValidationException exception) {
		return ResponseEntity.badRequest().body(PromotionApiResponse.validation(exception.getMessage()));
	}

	@ExceptionHandler(PromotionCodeConflictException.class)
	public ResponseEntity<PromotionApiResponse> handleConflict(PromotionCodeConflictException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(PromotionApiResponse.conflict(
				"PROMO_CODE_EXISTS",
				exception.getMessage(),
				new PromotionApiResponse.ConflictData(exception.getPromotionId())));
	}
}
