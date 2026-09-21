package com.team.home_service_app_server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank(message = "กรุณากรอกอีเมล") @Email(message = "รูปแบบอีเมลไม่ถูกต้อง") String email,
		@NotBlank(message = "กรุณากรอกรหัสผ่าน") String password
) {
}
