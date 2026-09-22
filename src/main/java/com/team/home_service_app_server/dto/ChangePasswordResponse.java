package com.team.home_service_app_server.dto;

public record ChangePasswordResponse(Object data, String message) {

	public static ChangePasswordResponse success() {
		return new ChangePasswordResponse(null, "เปลี่ยนรหัสผ่านสำเร็จ");
	}

}
