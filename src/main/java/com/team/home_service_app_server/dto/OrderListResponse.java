package com.team.home_service_app_server.dto;

import java.util.List;

public record OrderListResponse(String message, List<OrderCard> data) {

	public record OrderCard(
			Long jobId,
			String code,
			String status,
			String datetime,
			String staff,
			List<String> items,
			String total,
			Integer rating
	) {
	}

	public static OrderListResponse success(List<OrderCard> data) {
		return new OrderListResponse("Success", data);
	}
}
