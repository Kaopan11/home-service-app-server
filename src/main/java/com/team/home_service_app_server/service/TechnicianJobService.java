package com.team.home_service_app_server.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.technician.TechnicianJobDto;
import com.team.home_service_app_server.entity.JobStatus;
import com.team.home_service_app_server.entity.NotificationType;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceJob;
import com.team.home_service_app_server.entity.TechnicianProfile;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.BadRequestException;
import com.team.home_service_app_server.exception.ConflictException;
import com.team.home_service_app_server.repository.ServiceJobRepository;
import com.team.home_service_app_server.repository.TechnicianProfileRepository;

@Service
public class TechnicianJobService {

	private final UserService userService;
	private final TechnicianProfileRepository technicianProfileRepository;
	private final ServiceJobRepository serviceJobRepository;
	private final NotificationService notificationService;

	public TechnicianJobService(
			UserService userService,
			TechnicianProfileRepository technicianProfileRepository,
			ServiceJobRepository serviceJobRepository,
			NotificationService notificationService) {
		this.userService = userService;
		this.technicianProfileRepository = technicianProfileRepository;
		this.serviceJobRepository = serviceJobRepository;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public long countWaitingAccept() {
		Set<Long> serviceIds = acceptedServiceIds();
		if (serviceIds.isEmpty()) {
			return 0;
		}
		return serviceJobRepository.countWaitingAcceptByServiceIds(serviceIds);
	}

	@Transactional(readOnly = true)
	public List<TechnicianJobDto> listWaitingAccept() {
		Set<Long> serviceIds = acceptedServiceIds();
		if (serviceIds.isEmpty()) {
			return List.of();
		}
		return serviceJobRepository.findWaitingAcceptByServiceIds(serviceIds).stream()
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

	private Set<Long> acceptedServiceIds() {
		return acceptedServiceIds(userService.requireCurrentTechnician());
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
				job.getStatus().name());
	}
}
