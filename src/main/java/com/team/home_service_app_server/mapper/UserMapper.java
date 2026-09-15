package com.team.home_service_app_server.mapper;

import org.springframework.stereotype.Component;

import com.team.home_service_app_server.dto.UserDto;
import com.team.home_service_app_server.entity.User;

@Component
public class UserMapper {

	public UserDto toDto(User user) {
		return new UserDto(
				user.getUserId(),
				user.getEmail(),
				user.getFullName(),
				resolveDisplayName(user),
				user.getFirstName(),
				user.getLastName(),
				user.getPhone(),
				null,
				user.getAvatarUrl(),
				user.getRole().name());
	}

	private String resolveDisplayName(User user) {
		if (user.getFullName() != null && !user.getFullName().isBlank()) {
			return user.getFullName();
		}
		if (user.getFirstName() != null || user.getLastName() != null) {
			return String.join(" ",
					user.getFirstName() != null ? user.getFirstName() : "",
					user.getLastName() != null ? user.getLastName() : "").trim();
		}
		return user.getEmail();
	}

}
