package com.team.home_service_app_server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.home_service_app_server.dto.UpdateUserProfileRequest;
import com.team.home_service_app_server.dto.UserResponse;
import com.team.home_service_app_server.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public UserResponse me() {
		return UserResponse.success(userService.getCurrentUser());
	}

	@PutMapping("/me")
	public UserResponse updateProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
		return UserResponse.success(userService.updateCurrentUser(request));
	}

	@PatchMapping("/me")
	public UserResponse patchProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
		return UserResponse.success(userService.updateCurrentUser(request));
	}

}