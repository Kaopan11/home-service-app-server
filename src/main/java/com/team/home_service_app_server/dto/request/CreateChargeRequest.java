package com.team.home_service_app_server.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChargeRequest(
		@NotBlank(message = "กรุณาระบุ token การชำระเงิน") String token,
		@NotNull(message = "กรุณาระบุยอดชำระเงิน")
		@DecimalMin(value = "1", message = "ยอดชำระเงินไม่ถูกต้อง") BigDecimal amount,
		String description
) {
}
