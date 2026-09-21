package com.team.home_service_app_server.dto.category;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryApiResponse(
		boolean success,
		String message,
		Object data,
		String code,
		List<CategoryFieldError> errors
) {

	public record CategoryFieldError(String message) {
	}

	public record ConflictData(
			Integer id,
			@JsonProperty("is_active") boolean active
	) {
	}

	public static CategoryApiResponse list(List<CategoryDto> categories) {
		return new CategoryApiResponse(true, null, categories, null, null);
	}

	public static CategoryApiResponse item(CategoryDto category) {
		return new CategoryApiResponse(true, null, category, null, null);
	}

	public static CategoryApiResponse created(CategoryDto category) {
		return new CategoryApiResponse(true, "Category created successfully", category, null, null);
	}

	public static CategoryApiResponse updated(CategoryDto category) {
		return new CategoryApiResponse(true, "Category updated successfully", category, null, null);
	}

	public static CategoryApiResponse deleted() {
		return new CategoryApiResponse(true, "Category deleted successfully", null, null, null);
	}

	public static CategoryApiResponse notFound() {
		return new CategoryApiResponse(false, "Category not found", null, null, null);
	}

	public static CategoryApiResponse validation(String message) {
		return new CategoryApiResponse(false, null, null, null, List.of(new CategoryFieldError(message)));
	}

	public static CategoryApiResponse conflict(String code, String message, ConflictData data) {
		return new CategoryApiResponse(false, message, data, code, null);
	}

}
