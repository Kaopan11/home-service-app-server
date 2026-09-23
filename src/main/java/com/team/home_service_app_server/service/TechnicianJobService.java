package com.team.home_service_app_server.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.technician.TechnicianJobDetailDto;
import com.team.home_service_app_server.dto.technician.TechnicianJobDto;
import com.team.home_service_app_server.dto.technician.TechnicianJobItemDto;
import com.team.home_service_app_server.entity.JobStatus;
import com.team.home_service_app_server.entity.NotificationType;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceJob;
import com.team.home_service_app_server.entity.TechnicianJobDecline;
import com.team.home_service_app_server.entity.TechnicianProfile;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.BadRequestException;
import com.team.home_service_app_server.exception.ConflictException;
import com.team.home_service_app_server.repository.ServiceJobRepository;
import com.team.home_service_app_server.repository.TechnicianJobDeclineRepository;
import com.team.home_service_app_server.repository.TechnicianProfileRepository;

@Service
public class TechnicianJobService {

	private final UserService userService;
	private final TechnicianProfileRepository technicianProfileRepository;
	private final ServiceJobRepository serviceJobRepository;
	private final NotificationService notificationService;
	private final TechnicianJobDeclineRepository technicianJobDeclineRepository;

	public TechnicianJobService(
			UserService userService,
			TechnicianProfileRepository technicianProfileRepository,
			ServiceJobRepository serviceJobRepository,
			NotificationService notificationService,
			TechnicianJobDeclineRepository technicianJobDeclineRepository) {
		this.userService = userService;
		this.technicianProfileRepository = technicianProfileRepository;
		this.serviceJobRepository = serviceJobRepository;
		this.notificationService = notificationService;
		this.technicianJobDeclineRepository = technicianJobDeclineRepository;
	}

	@Transactional(readOnly = true)
	public long countWaitingAccept() {
		User technician = userService.requireCurrentTechnician();
		Set<Long> serviceIds = acceptedServiceIds(technician);
		if (serviceIds.isEmpty()) {
			return 0;
		}
		return serviceJobRepository.countWaitingAcceptByServiceIds(serviceIds, technician.getUserId());
	}

	@Transactional(readOnly = true)
	public List<TechnicianJobDto> listWaitingAccept() {
		User technician = userService.requireCurrentTechnician();
		Set<Long> serviceIds = acceptedServiceIds(technician);
		if (serviceIds.isEmpty()) {
			return List.of();
		}
		return serviceJobRepository.findWaitingAcceptByServiceIds(serviceIds, technician.getUserId()).stream()
				.map(this::toDto)
				.toList();
	}

	@Transactional
	public TechnicianJobDto accept(Long jobId) {
		User technician = userService.requireCurrentTechnician();
		Set<Long> serviceIds = acceptedServiceIds(technician);
		if (serviceIds.isEmpty()) {
			throw new BadRequestException("กรุณาเลือกบริการที่รับซ่อมก่อนรับงาน");
		}

		ServiceJob job = serviceJobRepository.findById(jobId)
				.orElseThrow(() -> new BadRequestException("ไม่พบคำขอบริการซ่อม"));
		if (job.getStatus() != JobStatus.WAITING_ACCEPT || job.getTechnician() != null) {
			throw new ConflictException("งานนี้ถูกรับไปแล้ว");
		}
		if (!serviceIds.contains(job.getService().getId())) {
			throw new BadRequestException("งานนี้ไม่อยู่ในบริการที่คุณรับซ่อม");
		}

		job.setTechnician(technician);
		job.setStatus(JobStatus.ACCEPTED);
		ServiceJob saved = serviceJobRepository.save(job);

		notificationService.notify(
				saved.getCustomer(),
				NotificationType.JOB_ACCEPTED,
				"ช่างรับงานของคุณแล้ว",
				"ช่าง " + technician.getFullName() + " รับคำขอบริการ \"" + saved.getService().getName() + "\" ของคุณแล้ว",
				saved);

		return toDto(saved);
	}

	@Transactional
	public void decline(Long jobId) {
		User technician = userService.requireCurrentTechnician();
		ServiceJob job = serviceJobRepository.findById(jobId)
				.orElseThrow(() -> new BadRequestException("ไม่พบคำขอบริการซ่อม"));
		if (job.getStatus() != JobStatus.WAITING_ACCEPT || job.getTechnician() != null) {
			throw new ConflictException("ไม่สามารถปฏิเสธงานนี้ได้");
		}
		if (technicianJobDeclineRepository.existsByTechnician_UserIdAndJob_Id(technician.getUserId(), jobId)) {
			return;
		}

		TechnicianJobDecline decline = new TechnicianJobDecline();
		decline.setTechnician(technician);
		decline.setJob(job);
		technicianJobDeclineRepository.save(decline);
	}

	private Set<Long> acceptedServiceIds(User technician) {
		TechnicianProfile profile = technicianProfileRepository.findByUser_UserId(technician.getUserId())
				.orElse(null);
		if (profile == null || !profile.isAvailable()) {
			return Set.of();
		}
		return profile.getServices().stream()
				.map(ServiceItem::getId)
				.collect(Collectors.toSet());
	}

	@Transactional(readOnly = true)
	public List<TechnicianJobItemDto> listPendingJobs(String sort) {
		User technician = userService.requireCurrentTechnician();
		String sortMode = "soonest".equalsIgnoreCase(sort) ? "soonest" : "latest";
		return serviceJobRepository.findPendingByTechnician(technician.getUserId(), sortMode).stream()
				.map(this::toItemDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TechnicianJobItemDto> listHistoryJobs() {
		User technician = userService.requireCurrentTechnician();
		return serviceJobRepository.findHistoryByTechnician(technician.getUserId()).stream()
				.map(this::toItemDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public TechnicianJobDetailDto getJobDetail(Long jobId) {
		User technician = userService.requireCurrentTechnician();
		ServiceJob job = serviceJobRepository.findByIdAndTechnician(jobId, technician.getUserId())
				.orElseThrow(() -> new BadRequestException("ไม่พบข้อมูลคำสั่งซ่อม"));
		return toDetailDto(job);
	}

	@Transactional
	public TechnicianJobDetailDto completeJob(Long jobId) {
		User technician = userService.requireCurrentTechnician();
		ServiceJob job = serviceJobRepository.findByIdAndTechnician(jobId, technician.getUserId())
				.orElseThrow(() -> new BadRequestException("ไม่พบข้อมูลคำสั่งซ่อม"));
		if (job.getStatus() != JobStatus.ACCEPTED) {
			throw new BadRequestException("สามารถจบงานได้เฉพาะงานที่อยู่ในสถานะรอดำเนินการเท่านั้น");
		}
		job.setStatus(JobStatus.COMPLETED);
		ServiceJob saved = serviceJobRepository.save(job);
		return toDetailDto(saved);
	}

	private TechnicianJobItemDto toItemDto(ServiceJob job) {
		String customerName = job.getCustomer().getFullName();
		if (customerName == null || customerName.isBlank()) {
			customerName = job.getCustomer().getEmail();
		}
		String orderCode = job.getOrderCode();
		if (orderCode == null || orderCode.isBlank()) {
			orderCode = String.format("AD%08d", job.getId());
		}
		Long categoryId = job.getService().getCategory() != null && job.getService().getCategory().getCategoryId() != null
				? job.getService().getCategory().getCategoryId().longValue()
				: null;
		String categoryName = job.getService().getCategory() != null ? job.getService().getCategory().getName() : "";
		java.math.BigDecimal totalPrice = job.getTotalPrice() != null ? job.getTotalPrice() : java.math.BigDecimal.valueOf(1550);
		java.time.Instant scheduledAt = job.getScheduledAt() != null ? job.getScheduledAt() : job.getCreatedAt();

		return new TechnicianJobItemDto(
				job.getId(),
				orderCode,
				job.getService().getId(),
				job.getService().getName(),
				categoryId,
				categoryName,
				scheduledAt,
				totalPrice,
				job.getStatus().name(),
				job.getAddress(),
				customerName);
	}

	private TechnicianJobDetailDto toDetailDto(ServiceJob job) {
		String customerName = job.getCustomer().getFullName();
		if (customerName == null || customerName.isBlank()) {
			customerName = job.getCustomer().getEmail();
		}
		String customerPhone = job.getCustomer().getPhone() != null ? job.getCustomer().getPhone() : "";
		String orderCode = job.getOrderCode();
		if (orderCode == null || orderCode.isBlank()) {
			orderCode = String.format("AD%08d", job.getId());
		}
		Long categoryId = job.getService().getCategory() != null && job.getService().getCategory().getCategoryId() != null
				? job.getService().getCategory().getCategoryId().longValue()
				: null;
		String categoryName = job.getService().getCategory() != null ? job.getService().getCategory().getName() : "";
		java.math.BigDecimal totalPrice = job.getTotalPrice() != null ? job.getTotalPrice() : java.math.BigDecimal.valueOf(1550);
		java.time.Instant scheduledAt = job.getScheduledAt() != null ? job.getScheduledAt() : job.getCreatedAt();
		String itemsDescription = job.getItemsDescription() != null && !job.getItemsDescription().isBlank()
				? job.getItemsDescription()
				: job.getService().getName();

		return new TechnicianJobDetailDto(
				job.getId(),
				orderCode,
				job.getService().getId(),
				job.getService().getName(),
				categoryId,
				categoryName,
				itemsDescription,
				scheduledAt,
				job.getAddress(),
				job.getLatitude(),
				job.getLongitude(),
				totalPrice,
				customerName,
				customerPhone,
				job.getRating() != null ? job.getRating() : (job.getStatus() == JobStatus.COMPLETED ? 5 : null),
				job.getReviewComment(),
				job.getStatus().name(),
				job.getCreatedAt());
	}

	private TechnicianJobDto toDto(ServiceJob job) {
		String customerName = job.getCustomer().getFullName();
		if (customerName == null || customerName.isBlank()) {
			customerName = job.getCustomer().getEmail();
		}
		return new TechnicianJobDto(
				job.getId(),
				job.getService().getName(),
				customerName,
				job.getAddress(),
				job.getLatitude(),
				job.getLongitude(),
				job.getStatus().name(),
				job.getCreatedAt());
	}
}
