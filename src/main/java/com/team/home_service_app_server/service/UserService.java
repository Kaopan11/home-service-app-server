package com.team.home_service_app_server.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.team.home_service_app_server.dto.UserDto;
import com.team.home_service_app_server.exception.ForbiddenException;
import com.team.home_service_app_server.exception.UnauthorizedException;
import com.team.home_service_app_server.mapper.UserMapper;
import com.team.home_service_app_server.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public UserService(UserRepository userRepository, UserMapper userMapper) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
	}

	public UserDto requireAdmin() {
		UserDto user = getCurrentUser();
		if (user.role() == null || !"ADMIN".equals(user.role())) {
			throw new ForbiddenException();
		}
		return user;
	}

	public UserDto getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			throw new UnauthorizedException();
		}

		return userRepository.findByEmail(authentication.getName())
				.map(userMapper::toDto)
				.orElseThrow(UnauthorizedException::new);
	}

}
