package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.technician.RefreshTechnicianLocationRequest;
import com.team.home_service_app_server.dto.technician.TechnicianLocationResponse;
import com.team.home_service_app_server.dto.technician.TechnicianProfileResponse;
import com.team.home_service_app_server.dto.technician.UpdateTechnicianProfileRequest;
import com.team.home_service_app_server.service.TechnicianProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/technician")
public class TechnicianProfileController {

	private final TechnicianProfileService technicianProfileService;

	public TechnicianProfileController(TechnicianProfileService technicianProfileService) {
		this.technicianProfileService = technicianProfileService;
	}

	@GetMapping("/account")
	public TechnicianProfileResponse me() {
		return TechnicianProfileResponse.success(technicianProfileService.getProfile());
	}

	@PatchMapping("/account")
	public TechnicianProfileResponse update(@Valid @RequestBody UpdateTechnicianProfileRequest request) {
		return TechnicianProfileResponse.success(technicianProfileService.updateProfile(request));
	}

	@PostMapping("/account/location")
	public TechnicianLocationResponse refreshLocation(@Valid @RequestBody RefreshTechnicianLocationRequest request) {
		return TechnicianLocationResponse.success(
				technicianProfileService.refreshLocation(request.latitude(), request.longitude()));
	}

}
