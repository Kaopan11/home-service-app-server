package com.team.home_service_app_server.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateChargeRequest(
		@NotBlank(message = "กรุณาระบุ token การชำระเงิน") String token,
		@NotNull(message = "กรุณาระบุยอดชำระเงิน")
		@DecimalMin(value = "20", message = "ยอดชำระเงินต้องไม่ต่ำกว่า 20 บาท") BigDecimal amount,
		String description,
		@NotNull(message = "กรุณาระบุบริการ") Long serviceId,
		@NotBlank(message = "กรุณาระบุที่อยู่") String address,
		@NotBlank(message = "กรุณาระบุวันเวลา") String scheduledAt,
		@NotEmpty(message = "กรุณาเลือกรายการบริการ") @Valid List<Item> items
) {
	public record Item(
			@NotNull Long optionId,
			@NotNull @Min(1) Integer quantity
	) {
	}
}
