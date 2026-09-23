package com.team.home_service_app_server.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OmiseErrorResponse(String code, String message) {
}
