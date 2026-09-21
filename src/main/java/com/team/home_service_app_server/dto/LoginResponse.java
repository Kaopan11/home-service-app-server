package com.team.home_service_app_server.dto;

public record LoginResponse(String message, LoginData data) {

	public record LoginData(UserDto user, SessionDto session) {
	}

	public static LoginResponse success(UserDto user, SessionDto session) {
		return new LoginResponse("เข้าสู่ระบบสำเร็จ", new LoginData(user, session));
	}

	public static LoginResponse registered(UserDto user, SessionDto session) {
		return new LoginResponse("ลงทะเบียนสำเร็จ", new LoginData(user, session));
	}

}
