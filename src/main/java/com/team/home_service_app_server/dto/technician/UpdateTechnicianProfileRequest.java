package com.team.home_service_app_server.dto.technician;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTechnicianProfileRequest(
		@NotBlank(message = "กรุณากรอกชื่อ") String firstName,
		@NotBlank(message = "กรุณากรอกนามสกุล") String lastName,
		@NotBlank(message = "กรุณากรอกเบอร์ติดต่อ") String phone,
		@NotBlank(message = "กรุณารีเฟรชตำแหน่งที่อยู่ปัจจุบัน") String address,
		Double latitude,
		Double longitude,
		@NotNull(message = "กรุณาเลือกสถานะบัญชี") Boolean available,
		@NotNull(message = "กรุณาเลือกบริการที่รับซ่อม") List<Long> serviceIds
) {
}
