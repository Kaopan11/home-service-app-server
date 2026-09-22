package com.team.home_service_app_server.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.team.home_service_app_server.dto.ServiceDetailDto;
import com.team.home_service_app_server.dto.ServiceListItemDto;
import com.team.home_service_app_server.dto.ServiceOptionDto;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceOptionItem;
import com.team.home_service_app_server.repository.ServiceItemRepository;
import com.team.home_service_app_server.repository.ServiceOptionRepository;

@Service
public class CatalogService {

	private final ServiceItemRepository serviceItemRepository;
	private final ServiceOptionRepository serviceOptionRepository;

	public CatalogService(
			ServiceItemRepository serviceItemRepository,
			ServiceOptionRepository serviceOptionRepository) {
		this.serviceItemRepository = serviceItemRepository;
		this.serviceOptionRepository = serviceOptionRepository;
	}

	@Transactional(readOnly = true)
	public List<ServiceListItemDto> list() {
		Map<Long, BigDecimal> minPriceByService = minPriceByServiceId();
		return serviceItemRepository.findAllWithCategoryOrderBySortOrderAsc().stream()
				.map(item -> toListDto(item, minPriceByService.get(item.getId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public ServiceDetailDto getById(long serviceId) {
		ServiceItem item = serviceItemRepository.findWithCategoryById(serviceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
		List<ServiceOptionDto> options = serviceOptionRepository.findByService_IdOrderByIdAsc(serviceId).stream()
				.map(this::toOptionDto)
				.toList();
		return new ServiceDetailDto(
				item.getId(),
				item.getName(),
				categoryName(item),
				blankToEmpty(item.getImageUrl()),
				options);
	}

	private Map<Long, BigDecimal> minPriceByServiceId() {
		Map<Long, BigDecimal> prices = new HashMap<>();
		for (Object[] row : serviceOptionRepository.findMinPriceByServiceId()) {
			if (row[0] == null || row[1] == null) {
				continue;
			}
			prices.put(toLong(row[0]), toBigDecimal(row[1]));
		}
		return prices;
	}

	private ServiceListItemDto toListDto(ServiceItem item, BigDecimal priceMin) {
		return new ServiceListItemDto(
				item.getId(),
				item.getName(),
				categoryName(item),
				item.getSortOrder(),
				blankToEmpty(item.getImageUrl()),
				priceMin == null ? BigDecimal.ZERO : priceMin);
	}

	private static String categoryName(ServiceItem item) {
		return item.getCategory() == null ? "" : item.getCategory().getName();
	}

	private ServiceOptionDto toOptionDto(ServiceOptionItem option) {
		BigDecimal price = option.getPrice() == null ? BigDecimal.ZERO : option.getPrice();
		String unit = option.getUnit() == null || option.getUnit().isBlank() ? "ชิ้น" : option.getUnit();
		return new ServiceOptionDto(option.getId(), option.getName(), unit, price);
	}

	private static String blankToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static Long toLong(Object value) {
		if (value instanceof Long id) {
			return id;
		}
		return ((Number) value).longValue();
	}

	private static BigDecimal toBigDecimal(Object value) {
		if (value instanceof BigDecimal amount) {
			return amount;
		}
		return new BigDecimal(value.toString());
	}
}
