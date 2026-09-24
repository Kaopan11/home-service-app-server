package com.team.home_service_app_server.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.OrderListResponse.OrderCard;
import com.team.home_service_app_server.entity.CustomerOrder;
import com.team.home_service_app_server.entity.OrderAssignment;
import com.team.home_service_app_server.entity.OrderItem;
import com.team.home_service_app_server.entity.ServiceOption;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.BadRequestException;
import com.team.home_service_app_server.repository.OrderRepository;

@Service
public class CustomerOrderService {

	private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

	private final OrderRepository orderRepository;
	private final UserService userService;

	public CustomerOrderService(OrderRepository orderRepository, UserService userService) {
		this.orderRepository = orderRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<OrderCard> listCurrentUser(String scope) {
		boolean history = isHistory(scope);
		Long userId = userService.requireCurrentUserEntity().getUserId();
		List<CustomerOrder> orders = orderRepository.findCardsByCustomerId(userId);
		Map<Long, OrderAssignment> assignmentByOrderId = latestAssignmentByOrderId(orders);

		List<OrderCard> cards = new ArrayList<>();
		for (CustomerOrder order : orders) {
			String status = uiStatus(order.getStatus());
			if (history != "done".equals(status)) {
				continue;
			}
			OrderAssignment assignment = assignmentByOrderId.get(order.getId());
			Instant when = history
					? firstNonNull(assignment == null ? null : assignment.getCompletedAt(), order.getScheduledAt(), order.getCreatedAt())
					: firstNonNull(order.getScheduledAt(), order.getCreatedAt());
			cards.add(new OrderCard(
					order.getOrderCode() == null ? "" : order.getOrderCode(),
					status,
					formatWhen(when, history),
					staffName(assignment),
					order.getItems().stream().map(CustomerOrderService::formatItem).toList(),
					formatTotal(order.getTotalPrice())));
		}
		return cards;
	}

	static boolean isHistory(String scope) {
		if (scope == null || scope.isBlank() || "active".equals(scope)) {
			return false;
		}
		if ("history".equals(scope)) {
			return true;
		}
		throw new BadRequestException("scope ต้องเป็น active หรือ history");
	}

	static String uiStatus(String raw) {
		if (raw == null || raw.isBlank()) {
			return "pending";
		}
		return switch (raw.trim().toLowerCase().replace('-', '_')) {
			case "done", "completed", "complete", "success", "successful" -> "done";
			case "progress", "in_progress", "processing", "accepted", "assigned", "ongoing" -> "progress";
			default -> "pending";
		};
	}

	static String formatWhen(Instant instant, boolean history) {
		String label = history ? "วันเวลาดำเนินการสำเร็จ" : "วันเวลาดำเนินการ";
		if (instant == null) {
			return label + ": -";
		}
		ZonedDateTime zoned = instant.atZone(BANGKOK);
		int buddhistYear = zoned.getYear() + 543;
		return "%s: %02d/%02d/%d เวลา %02d.%02d น.".formatted(
				label,
				zoned.getDayOfMonth(),
				zoned.getMonthValue(),
				buddhistYear,
				zoned.getHour(),
				zoned.getMinute());
	}

	static String formatTotal(BigDecimal total) {
		DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
		return format.format(total == null ? BigDecimal.ZERO : total) + " ฿";
	}

	static String formatItem(OrderItem item) {
		ServiceOption option = item.getOption();
		String name = option == null || option.getOptionName() == null ? "" : option.getOptionName().trim();
		int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
		String unit = option == null || option.getUnit() == null ? "" : option.getUnit().trim();
		return (name + " " + quantity + " " + unit).trim().replaceAll(" +", " ");
	}

	private Map<Long, OrderAssignment> latestAssignmentByOrderId(List<CustomerOrder> orders) {
		Map<Long, OrderAssignment> latest = new LinkedHashMap<>();
		if (orders.isEmpty()) {
			return latest;
		}
		List<Long> ids = orders.stream().map(CustomerOrder::getId).toList();
		for (OrderAssignment assignment : orderRepository.findAssignmentsByOrderIds(ids)) {
			latest.putIfAbsent(assignment.getCustomerOrder().getId(), assignment);
		}
		return latest;
	}

	private static String staffName(OrderAssignment assignment) {
		if (assignment == null || assignment.getTechnician() == null) {
			return "";
		}
		User user = assignment.getTechnician().getUser();
		if (user == null) {
			return "";
		}
		if (user.getFullName() != null && !user.getFullName().isBlank()) {
			return user.getFullName().trim();
		}
		return String.join(" ",
				user.getFirstName() == null ? "" : user.getFirstName(),
				user.getLastName() == null ? "" : user.getLastName()).trim();
	}

	private static Instant firstNonNull(Instant... values) {
		for (Instant value : values) {
			if (value != null) {
				return value;
			}
		}
		return null;
	}
}
