package com.team.home_service_app_server.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "กรุณากรอกชื่อ นามสกุล") String fullName,
		@NotBlank(message = "กรุณากรอกเบอร์โทรศัพท์") String phone,
		@NotBlank(message = "กรุณากรอกอีเมล") @Email(message = "รูปแบบอีเมลไม่ถูกต้อง") String email,
		@NotBlank(message = "กรุณากรอกรหัสผ่าน") @Size(min = 6, message = "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร") String password,
		@AssertTrue(message = "กรุณายอมรับข้อตกลงและเงื่อนไข") boolean accepted
) {
}
