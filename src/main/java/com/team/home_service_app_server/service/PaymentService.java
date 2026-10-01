package com.team.home_service_app_server.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.team.home_service_app_server.client.OmiseClient;
import com.team.home_service_app_server.client.dto.OmiseChargeResponse;
import com.team.home_service_app_server.dto.response.ChargeResponse;
import com.team.home_service_app_server.exception.PaymentFailedException;

@Service
public class PaymentService {

	private static final String CURRENCY = "thb";

	// Omise's minimum chargeable amount for THB is 20 baht.
	private static final BigDecimal MIN_AMOUNT_BAHT = BigDecimal.valueOf(20);

	private final OmiseClient omiseClient;

	public PaymentService(OmiseClient omiseClient) {
		this.omiseClient = omiseClient;
	}

	public ChargeResponse.ChargeData charge(String token, BigDecimal amountBaht, String description) {
		if (amountBaht.compareTo(MIN_AMOUNT_BAHT) < 0) {
			throw new PaymentFailedException("จำนวนเงินต้องไม่ต่ำกว่า 20 บาท");
		}

		long amountSatang = amountBaht
				.multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_UP)
				.longValueExact();

		OmiseChargeResponse response = omiseClient.createCharge(token, amountSatang, CURRENCY, description);

		if (response == null) {
			throw new PaymentFailedException("ไม่ได้รับผลลัพธ์จาก Omise");
		}

		if (!response.paid() || !"successful".equals(response.status())) {
			String reason = response.failureMessage() != null && !response.failureMessage().isBlank()
					? response.failureMessage()
					: "การชำระเงินไม่สำเร็จ";
			throw new PaymentFailedException(reason);
		}

		return new ChargeResponse.ChargeData(
				response.id(),
				response.status(),
				response.paid(),
				response.amount(),
				response.currency());
	}

}
