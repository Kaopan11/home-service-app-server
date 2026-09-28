package com.team.home_service_app_server.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.OrderListResponse.OrderCard;
import com.team.home_service_app_server.dto.request.CreateChargeRequest;
import com.team.home_service_app_server.dto.request.SubmitReviewRequest;
import com.team.home_service_app_server.entity.CustomerOrder;
import com.team.home_service_app_server.entity.JobStatus;
import com.team.home_service_app_server.entity.NotificationType;
import com.team.home_service_app_server.entity.OrderItem;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceJob;
import com.team.home_service_app_server.entity.ServiceOption;
import com.team.home_service_app_server.entity.ServiceOptionItem;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.BadRequestException;
import com.team.home_service_app_server.exception.ConflictException;
import com.team.home_service_app_server.exception.ForbiddenException;
import com.team.home_service_app_server.repository.OrderRepository;
import com.team.home_service_app_server.repository.ServiceItemRepository;
import com.team.home_service_app_server.repository.ServiceJobRepository;
import com.team.home_service_app_server.repository.ServiceOptionRepository;

@Service
public class CustomerOrderService {

	private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

	private final OrderRepository orderRepository;
	private final ServiceJobRepository serviceJobRepository;
	private final ServiceItemRepository serviceItemRepository;
	private final ServiceOptionRepository serviceOptionRepository;
	private final UserService userService;
	private final NotificationService notificationService;
	private final EntityManager entityManager;

	public CustomerOrderService(
			OrderRepository orderRepository,
			ServiceJobRepository serviceJobRepository,
			ServiceItemRepository serviceItemRepository,
			ServiceOptionRepository serviceOptionRepository,
			UserService userService,
			NotificationService notificationService,
			EntityManager entityManager) {
		this.orderRepository = orderRepository;
		this.serviceJobRepository = serviceJobRepository;
		this.serviceItemRepository = serviceItemRepository;
		this.serviceOptionRepository = serviceOptionRepository;
		this.userService = userService;
		this.notificationService = notificationService;
		this.entityManager = entityManager;
	}

	public record PaidBooking(
			User customer,
			ServiceItem service,
			Instant scheduledAt,
			String address,
			List<Line> lines) {

		public record Line(ServiceOptionItem option, int quantity) {
		}
	}

	@Transactional(readOnly = true)
	public PaidBooking draft(CreateChargeRequest request) {
		User customer = userService.requireCurrentUserEntity();
		ServiceItem service = serviceItemRepository.findById(request.serviceId())
				.orElseThrow(() -> new BadRequestException("ไม่พบบริการ"));
		List<PaidBooking.Line> lines = new ArrayList<>();
		for (CreateChargeRequest.Item item : request.items()) {
			ServiceOptionItem option = serviceOptionRepository.findById(item.optionId())
					.orElseThrow(() -> new BadRequestException("ไม่พบรายการบริการ"));
			if (!service.getId().equals(option.getService().getId())) {
				throw new BadRequestException("รายการไม่ตรงกับบริการที่จอง");
			}
			lines.add(new PaidBooking.Line(option, item.quantity()));
		}
		return new PaidBooking(customer, service, parseScheduledAt(request.scheduledAt()), request.address().trim(), lines);
	}

	@Transactional
	public void savePaidBooking(PaidBooking booking, BigDecimal total) {
		String code = "AD" + String.format("%08d", System.currentTimeMillis() % 100000000L);
		CustomerOrder order = new CustomerOrder();
		order.setOrderCode(code);
		order.setCustomer(booking.customer());
		order.setService(booking.service());
		order.setTotalPrice(total);
		order.setScheduledAt(booking.scheduledAt());
		order.setCreatedAt(Instant.now());
		for (PaidBooking.Line line : booking.lines()) {
			OrderItem item = new OrderItem();
			item.setCustomerOrder(order);
			item.setOption(entityManager.getReference(ServiceOption.class, line.option().getId()));
			item.setQuantity(line.quantity());
			item.setUnitPrice(line.option().getPrice());
			order.getItems().add(item);
		}
		CustomerOrder saved = orderRepository.save(order);

		ServiceJob job = new ServiceJob();
		job.setCustomer(booking.customer());
		job.setService(booking.service());
		job.setCustomerOrder(saved);
		job.setAddress(booking.address());
		job.setStatus(JobStatus.WAITING_ACCEPT);
		job.setOrderCode(code);
		job.setTotalPrice(total);
		job.setScheduledAt(booking.scheduledAt());
		job.setItemsDescription(String.join(", ", booking.lines().stream()
				.map(line -> (line.option().getName() + " " + line.quantity() + " " + line.option().getUnit()).trim())
				.toList()));
		ServiceJob savedJob = serviceJobRepository.save(job);
		String serviceName = booking.service().getName();
		notificationService.notify(
				booking.customer(),
				NotificationType.JOB_CREATED,
				"ได้รับคำสั่งซ่อมแล้ว",
				"คำสั่งซ่อม \"" + serviceName + "\" ของคุณอยู่ระหว่างรอช่างรับงาน",
				savedJob);
		notificationService.notifyTechnicians(
				NotificationType.JOB_CREATED,
				"มีคำสั่งซ่อมใหม่",
				"ลูกค้าสร้างคำสั่งซ่อม \"" + serviceName + "\"",
				savedJob);
	}

	@Transactional(readOnly = true)
	public List<OrderCard> listCurrentUser(String scope) {
		boolean history = isHistory(scope);
		Long userId = userService.requireCurrentUserEntity().getUserId();
		List<CustomerOrder> orders = orderRepository.findCardsByCustomerId(userId);
		Map<Long, ServiceJob> jobByOrderId = jobsByOrderId(orders);

		List<OrderCard> cards = new ArrayList<>();
		for (CustomerOrder order : orders) {
			ServiceJob job = jobByOrderId.get(order.getId());
			if (job == null || job.getStatus() == JobStatus.CANCELLED) {
				continue;
			}
			String status = uiStatus(job.getStatus().name());
			if (history != "done".equals(status)) {
				continue;
			}
			Instant when = history
					? firstNonNull(job.getUpdatedAt(), order.getScheduledAt(), order.getCreatedAt())
					: firstNonNull(order.getScheduledAt(), order.getCreatedAt());
			cards.add(new OrderCard(
					job.getId(),
					order.getOrderCode() == null ? "" : order.getOrderCode(),
					status,
					formatWhen(when, history),
					staffName(job.getTechnician()),
					order.getItems().stream().map(CustomerOrderService::formatItem).toList(),
					formatTotal(order.getTotalPrice()),
					job.getRating()));
		}
		return cards;
	}

	@Transactional
	public OrderCard submitReview(Long jobId, SubmitReviewRequest request) {
		User customer = userService.requireCurrentUserEntity();
		ServiceJob job = serviceJobRepository.findById(jobId)
				.orElseThrow(() -> new BadRequestException("ไม่พบคำสั่งซ่อม"));
		if (!job.getCustomer().getUserId().equals(customer.getUserId())) {
			throw new ForbiddenException("ไม่มีสิทธิ์รีวิวคำสั่งซ่อมนี้");
		}
		if (job.getStatus() != JobStatus.COMPLETED) {
			throw new BadRequestException("รีวิวได้เฉพาะงานที่ดำเนินการสำเร็จ");
		}
		if (job.getRating() != null) {
			throw new ConflictException("รีวิวคำสั่งซ่อมนี้อยู่แล้ว");
		}

		int rating = request.rating();
		String comment = request.comment() == null ? null : request.comment().trim();
		if (comment != null && comment.length() > 500) {
			throw new BadRequestException("ความคิดเห็นต้องไม่เกิน 500 ตัวอักษร");
		}
		if (comment != null && comment.isBlank()) {
			comment = null;
		}

		job.setRating(rating);
		job.setReviewComment(comment);
		ServiceJob saved = serviceJobRepository.save(job);

		User technician = saved.getTechnician();
		if (technician != null) {
			String serviceName = saved.getService().getName();
			notificationService.notify(
					technician,
					NotificationType.JOB_REVIEWED,
					"มีรีวิวจากผู้รับบริการ",
					"ลูกค้าให้คะแนน " + rating + " ดาว สำหรับ \"" + serviceName + "\"",
					saved);
		}

		Instant when = firstNonNull(saved.getUpdatedAt(), saved.getScheduledAt(), saved.getCreatedAt());
		String code = saved.getOrderCode() == null ? "" : saved.getOrderCode();
		CustomerOrder order = saved.getCustomerOrder();
		if (order != null && order.getOrderCode() != null) {
			code = order.getOrderCode();
		}
		return new OrderCard(
				saved.getId(),
				code,
				"done",
				formatWhen(when, true),
				staffName(saved.getTechnician()),
				List.of(),
				formatTotal(order == null ? saved.getTotalPrice() : order.getTotalPrice()),
				saved.getRating());
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

	private Map<Long, ServiceJob> jobsByOrderId(List<CustomerOrder> orders) {
		Map<Long, ServiceJob> jobs = new LinkedHashMap<>();
		if (orders.isEmpty()) {
			return jobs;
		}
		List<Long> ids = orders.stream().map(CustomerOrder::getId).toList();
		for (ServiceJob job : serviceJobRepository.findByOrderIds(ids)) {
			jobs.putIfAbsent(job.getCustomerOrder().getId(), job);
		}
		return jobs;
	}

	private static Instant parseScheduledAt(String raw) {
		try {
			return LocalDateTime.parse(raw).atZone(BANGKOK).toInstant();
		} catch (DateTimeParseException exception) {
			throw new BadRequestException("วันเวลาที่จองไม่ถูกต้อง");
		}
	}

	private static String staffName(User user) {
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
