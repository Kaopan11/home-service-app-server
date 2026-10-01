package com.team.home_service_app_server.dto.promo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplyPromoRequest(String code, BigDecimal amount) {
}
