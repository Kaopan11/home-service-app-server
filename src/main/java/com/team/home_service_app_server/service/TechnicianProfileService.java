package com.team.home_service_app_server.service;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team.home_service_app_server.dto.technician.TechnicianLocationDto;
import com.team.home_service_app_server.dto.technician.TechnicianProfileDto;
import com.team.home_service_app_server.dto.technician.TechnicianServiceOptionDto;
import com.team.home_service_app_server.dto.technician.UpdateTechnicianProfileRequest;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.TechnicianProfile;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.exception.BadRequestException;
import com.team.home_service_app_server.exception.ConflictException;
import com.team.home_service_app_server.mapper.UserMapper;
import com.team.home_service_app_server.repository.ServiceItemRepository;
import com.team.home_service_app_server.repository.TechnicianProfileRepository;
import com.team.home_service_app_server.repository.UserRepository;

@Service
public class TechnicianProfileService {

	private final UserService userService;
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final TechnicianProfileRepository technicianProfileRepository;
	private final ServiceItemRepository serviceItemRepository;
	private final GeoLocationService geoLocationService;

	public TechnicianProfileService(
			UserService userService,
			UserRepository userRepository,
			UserMapper userMapper,
			TechnicianProfileRepository technicianProfileRepository,
			ServiceItemRepository serviceItemRepository,
			GeoLocationService geoLocationService) {
		this.userService = userService;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.technicianProfileRepository = technicianProfileRepository;
		this.serviceItemRepository = serviceItemRepository;
		this.geoLocationService = geoLocationService;
	}

	@Transactional
	public TechnicianProfileDto getProfile() {
		User user = userService.requireCurrentTechnician();
		TechnicianProfile profile = getOrCreate(user);
		return toDto(user, profile);
	}

	@Transactional
	public TechnicianProfileDto updateProfile(UpdateTechnicianProfileRequest request) {
		User user = userService.requireCurrentTechnician();
		String phone = request.phone().trim();
		if (userRepository.existsByPhoneAndUserIdNot(phone, user.getUserId())) {
			throw new ConflictException("เบอร์โทรศัพท์นี้ถูกใช้แล้ว");
		}

		List<ServiceItem> selectedServices = resolveServices(request.serviceIds());
		TechnicianProfile profile = getOrCreate(user);

		String firstName = request.firstName().trim();
		String lastName = request.lastName().trim();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setFullName((firstName + " " + lastName).trim());
		user.setPhone(phone);
		user.setUpdatedAt(Instant.now());
		userRepository.save(user);

		profile.setAddress(request.address().trim());
		profile.setLatitude(request.latitude());
		profile.setLongitude(request.longitude());
		profile.setAvailable(Boolean.TRUE.equals(request.available()));
		profile.getServices().clear();
		profile.getServices().addAll(selectedServices);
		technicianProfileRepository.save(profile);

		return toDto(user, profile);
	}

	@Transactional(readOnly = true)
	public TechnicianLocationDto refreshLocation(double latitude, double longitude) {
		userService.requireCurrentTechnician();
		String address = geoLocationService.reverseGeocode(latitude, longitude);
		return new TechnicianLocationDto(address, latitude, longitude);
	}

	private TechnicianProfile getOrCreate(User user) {
		return technicianProfileRepository.findByUser_UserId(user.getUserId()).orElseGet(() -> {
			TechnicianProfile profile = new TechnicianProfile();
			profile.setUser(user);
			profile.setAvailable(true);
			return technicianProfileRepository.save(profile);
		});
	}

	private List<ServiceItem> resolveServices(List<Long> serviceIds) {
		List<Long> uniqueIds = serviceIds == null
				? List.of()
				: serviceIds.stream().filter(id -> id != null).distinct().toList();
		if (uniqueIds.isEmpty()) {
			return List.of();
		}
		List<ServiceItem> items = serviceItemRepository.findAllById(uniqueIds);
		if (items.size() != uniqueIds.size()) {
			throw new BadRequestException("พบบริการที่ไม่ถูกต้อง");
		}
		return items;
	}

	private TechnicianProfileDto toDto(User user, TechnicianProfile profile) {
		List<TechnicianServiceOptionDto> catalog = serviceItemRepository.findAllByActiveTrueOrderBySortOrderAsc().stream()
				.map(item -> new TechnicianServiceOptionDto(item.getId(), item.getName()))
				.toList();
		Set<Long> accepted = new LinkedHashSet<>();
		for (ServiceItem item : profile.getServices()) {
			accepted.add(item.getId());
		}
		return new TechnicianProfileDto(
				user.getUserId(),
				user.getEmail(),
				user.getFullName(),
				userMapper.toDto(user).displayName(),
				user.getFirstName(),
				user.getLastName(),
				user.getPhone(),
				profile.getAddress(),
				user.getAvatarUrl(),
				user.getRole().name(),
				profile.getLatitude(),
				profile.getLongitude(),
				profile.isAvailable(),
				List.copyOf(accepted),
				catalog);
	}
}
