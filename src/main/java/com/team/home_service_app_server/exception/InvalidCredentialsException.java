package com.team.home_service_app_server.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("อีเมลหรือรหัสผ่านไม่ถูกต้อง");
	}

	public InvalidCredentialsException(String message) {
		super(message);
	}

}
