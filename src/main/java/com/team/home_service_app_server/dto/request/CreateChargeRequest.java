package com.team.home_service_app_server.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChargeRequest(
		@NotBlank(message = "กรุณาระบุ token การชำระเงิน") String token,
		@NotNull(message = "กรุณาระบุยอดชำระเงิน")
		@DecimalMin(value = "20", message = "ยอดชำระเงินต้องไม่ต่ำกว่า 20 บาท") BigDecimal amount,
		String description
) {
}
