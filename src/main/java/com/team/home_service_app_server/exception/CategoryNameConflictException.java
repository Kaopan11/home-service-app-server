package com.team.home_service_app_server.exception;

public class CategoryNameConflictException extends RuntimeException {

	private final String code;
	private final int categoryId;
	private final boolean active;

	public CategoryNameConflictException(String code, String message, int categoryId, boolean active) {
		super(message);
		this.code = code;
		this.categoryId = categoryId;
		this.active = active;
	}

	public String getCode() {
		return code;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public boolean isActive() {
		return active;
	}

}
