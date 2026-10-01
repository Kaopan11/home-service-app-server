package com.team.home_service_app_server.dto.response;

public record ChargeResponse(String message, ChargeData data) {

	public static ChargeResponse success(ChargeData data) {
		return new ChargeResponse("Success", data);
	}

	public record ChargeData(
			String id,
			String status,
			boolean paid,
			long amount,
			String currency
	) {
	}

}
