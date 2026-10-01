package com.team.home_service_app_server.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitReviewRequest(
		@NotNull(message = "กรุณาให้คะแนน")
		@Min(value = 1, message = "คะแนนต้องอยู่ระหว่าง 1 ถึง 5")
		@Max(value = 5, message = "คะแนนต้องอยู่ระหว่าง 1 ถึง 5")
		Integer rating,
		String comment
) {
}
