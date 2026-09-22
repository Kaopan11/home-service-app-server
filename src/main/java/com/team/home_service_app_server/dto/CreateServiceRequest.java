package com.team.home_service_app_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateServiceRequest(String name, @JsonProperty("category_id") Integer categoryId) {
}
