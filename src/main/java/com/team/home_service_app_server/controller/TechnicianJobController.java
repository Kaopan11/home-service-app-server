package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.MessageResponse;
import com.team.home_service_app_server.dto.technician.TechnicianJobListResponse;
import com.team.home_service_app_server.dto.technician.TechnicianJobResponse;
import com.team.home_service_app_server.dto.technician.TechnicianPendingCountResponse;
import com.team.home_service_app_server.service.TechnicianJobService;

@RestController
@RequestMapping("/api/technician/requests")
public class TechnicianJobController {

	private final TechnicianJobService technicianJobService;

	public TechnicianJobController(TechnicianJobService technicianJobService) {
		this.technicianJobService = technicianJobService;
	}

	@GetMapping("/pending-count")
	public TechnicianPendingCountResponse pendingCount() {
		return TechnicianPendingCountResponse.success(technicianJobService.countWaitingAccept());
	}

	@GetMapping
	public TechnicianJobListResponse listWaiting() {
		return TechnicianJobListResponse.success(technicianJobService.listWaitingAccept());
	}

	@PostMapping("/{id}/accept")
	public TechnicianJobResponse accept(@PathVariable Long id) {
		return TechnicianJobResponse.success(technicianJobService.accept(id));
	}

	@PostMapping("/{id}/decline")
	public MessageResponse decline(@PathVariable Long id) {
		technicianJobService.decline(id);
		return new MessageResponse("ปฏิเสธคำขอบริการแล้ว");
	}
}
