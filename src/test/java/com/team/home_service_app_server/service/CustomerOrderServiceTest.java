package com.team.home_service_app_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.team.home_service_app_server.exception.BadRequestException;

class CustomerOrderServiceTest {

	@Test
	void mapsDatabaseStatusesToCardStatuses() {
		assertEquals("pending", CustomerOrderService.uiStatus("PENDING"));
		assertEquals("progress", CustomerOrderService.uiStatus("IN_PROGRESS"));
		assertEquals("done", CustomerOrderService.uiStatus("COMPLETED"));
	}

	@Test
	void formatsBuddhistDatetimeLikeTheOrderCard() {
		Instant at = Instant.parse("2020-04-25T06:00:00Z");
		assertEquals("วันเวลาดำเนินการ: 25/04/2563 เวลา 13.00 น.", CustomerOrderService.formatWhen(at, false));
		assertEquals("วันเวลาดำเนินการสำเร็จ: 25/04/2563 เวลา 13.00 น.", CustomerOrderService.formatWhen(at, true));
	}

	@Test
	void formatsTotalWithCommaAndBaht() {
		assertEquals("1,550.00 ฿", CustomerOrderService.formatTotal(new BigDecimal("1550")));
	}

	@Test
	void scopeHistoryIsOnlyTheHistoryFlag() {
		assertFalse(CustomerOrderService.isHistory("active"));
		assertTrue(CustomerOrderService.isHistory("history"));
		assertThrows(BadRequestException.class, () -> CustomerOrderService.isHistory("all"));
	}
}
