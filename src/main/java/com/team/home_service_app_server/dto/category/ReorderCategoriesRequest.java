package com.team.home_service_app_server.dto.category;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReorderCategoriesRequest(List<Integer> ids) {
}
