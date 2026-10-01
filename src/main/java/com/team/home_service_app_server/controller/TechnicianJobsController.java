package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.technician.TechnicianJobDetailResponse;
import com.team.home_service_app_server.dto.technician.TechnicianJobItemListResponse;
import com.team.home_service_app_server.service.TechnicianJobService;

@RestController
@RequestMapping("/api/technician/jobs")
public class TechnicianJobsController {

	private final TechnicianJobService technicianJobService;

	public TechnicianJobsController(TechnicianJobService technicianJobService) {
		this.technicianJobService = technicianJobService;
	}

	@GetMapping("/pending")
	public TechnicianJobItemListResponse listPending(@RequestParam(value = "sort", required = false) String sort) {
		return TechnicianJobItemListResponse.success(technicianJobService.listPendingJobs(sort));
	}

	@GetMapping("/history")
	public TechnicianJobItemListResponse listHistory() {
		return TechnicianJobItemListResponse.success(technicianJobService.listHistoryJobs());
	}

	@GetMapping("/{id}")
	public TechnicianJobDetailResponse getDetail(@PathVariable Long id) {
		return TechnicianJobDetailResponse.success(technicianJobService.getJobDetail(id));
	}

	@PostMapping("/{id}/complete")
	public TechnicianJobDetailResponse complete(@PathVariable Long id) {
		return TechnicianJobDetailResponse.success(technicianJobService.completeJob(id));
	}
}
