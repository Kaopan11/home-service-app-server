package com.team.home_service_app_server.exception;

public class ForbiddenException extends RuntimeException {

	public ForbiddenException() {
		super("บัญชีนี้ไม่มีสิทธิ์เข้าถึงระบบ Admin");
	}

	public ForbiddenException(String message) {
		super(message);
	}

}
