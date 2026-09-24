package com.team.home_service_app_server.dto;

import java.util.List;

public record OrderListResponse(String message, List<OrderCard> data) {

	public record OrderCard(
			String code,
			String status,
			String datetime,
			String staff,
			List<String> items,
			String total
	) {
	}

	public static OrderListResponse success(List<OrderCard> data) {
		return new OrderListResponse("Success", data);
	}
}
