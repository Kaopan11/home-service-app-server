package com.team.home_service_app_server.dto;

import java.util.List;

public record ReorderServicesRequest(List<Long> ids) {
}
