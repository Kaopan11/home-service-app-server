package com.team.home_service_app_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import com.team.home_service_app_server.dto.CreateServiceRequest;
import com.team.home_service_app_server.dto.ServiceOptionRequest;
import com.team.home_service_app_server.entity.Category;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceOptionItem;
import com.team.home_service_app_server.repository.CategoryRepository;
import com.team.home_service_app_server.repository.ServiceItemRepository;
import com.team.home_service_app_server.repository.ServiceOptionRepository;

class AdminCatalogServiceTest {

	@Test
	void createPersistsImageAndOrderedOptions() {
		ServiceItemRepository services = mock(ServiceItemRepository.class);
		ServiceOptionRepository options = mock(ServiceOptionRepository.class);
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCatalogService catalog = new AdminCatalogService(services, options, categories, users);

		Category category = new Category();
		category.setCategoryId(1);
		category.setName("บริการทั่วไป");
		when(categories.findById(1)).thenReturn(Optional.of(category));
		when(services.findAllByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());
		when(services.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		catalog.create(new CreateServiceRequest(
				"ล้างแอร์",
				1,
				"data:image/png;base64,aGVsbG8=",
				List.of(
						new ServiceOptionRequest("เล็ก", new BigDecimal("500.00"), "เครื่อง", null),
						new ServiceOptionRequest("ใหญ่", new BigDecimal("800.00"), "เครื่อง", null)),
				null));

		ArgumentCaptor<ServiceOptionItem> savedOptions = ArgumentCaptor.forClass(ServiceOptionItem.class);
		verify(options, org.mockito.Mockito.times(2)).save(savedOptions.capture());
		assertEquals(List.of("เล็ก", "ใหญ่"), savedOptions.getAllValues().stream().map(ServiceOptionItem::getName).toList());
		assertEquals(List.of(1, 2), savedOptions.getAllValues().stream().map(ServiceOptionItem::getSortOrder).toList());
	}

	@Test
	void updateReplacesOptions() {
		ServiceItemRepository services = mock(ServiceItemRepository.class);
		ServiceOptionRepository options = mock(ServiceOptionRepository.class);
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCatalogService catalog = new AdminCatalogService(services, options, categories, users);

		Category category = new Category();
		category.setCategoryId(1);
		category.setName("บริการทั่วไป");
		ServiceItem existing = new ServiceItem();
		existing.setId(9L);
		existing.setName("ล้างแอร์");
		existing.setCategory(category);
		when(services.findById(9L)).thenReturn(Optional.of(existing));
		when(categories.findById(1)).thenReturn(Optional.of(category));
		when(services.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		catalog.update(9L, new com.team.home_service_app_server.dto.PatchServiceRequest(
				"ล้างแอร์",
				1,
				"data:image/png;base64,aGVsbG8=",
				List.of(new ServiceOptionRequest("ใหญ่", new BigDecimal("800.00"), "เครื่อง", null)),
				null));

		verify(options).deleteByService_Id(9L);
		verify(options).save(any(ServiceOptionItem.class));
	}

	@Test
	void deleteMarksServiceInactiveInsteadOfHardDelete() {
		ServiceItemRepository services = mock(ServiceItemRepository.class);
		ServiceOptionRepository options = mock(ServiceOptionRepository.class);
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCatalogService catalog = new AdminCatalogService(services, options, categories, users);

		ServiceItem existing = new ServiceItem();
		existing.setId(9L);
		existing.setActive(true);
		when(services.findById(9L)).thenReturn(Optional.of(existing));
		when(services.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		catalog.delete(9L);

		assertFalse(existing.isActive());
		verify(services).save(existing);
		verify(services, org.mockito.Mockito.never()).deleteById(9L);
	}

	@Test
	void createRejectsMissingOptions() {
		ServiceItemRepository services = mock(ServiceItemRepository.class);
		ServiceOptionRepository options = mock(ServiceOptionRepository.class);
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCatalogService catalog = new AdminCatalogService(services, options, categories, users);

		Category category = new Category();
		category.setCategoryId(1);
		when(categories.findById(1)).thenReturn(Optional.of(category));

		assertThrows(ResponseStatusException.class, () -> catalog.create(new CreateServiceRequest(
				"ล้างแอร์",
				1,
				"data:image/png;base64,aGVsbG8=",
				List.of(),
				null)));
	}

	@Test
	void createAcceptsJpgDataUrlAlias() {
		ServiceItemRepository services = mock(ServiceItemRepository.class);
		ServiceOptionRepository options = mock(ServiceOptionRepository.class);
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCatalogService catalog = new AdminCatalogService(services, options, categories, users);

		Category category = new Category();
		category.setCategoryId(1);
		when(categories.findById(1)).thenReturn(Optional.of(category));
		when(services.findAllByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of());
		when(services.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

		catalog.create(new CreateServiceRequest(
				"ล้างแอร์",
				1,
				"data:image/jpg;base64,aGVsbG8=",
				List.of(new ServiceOptionRequest("เล็ก", new BigDecimal("500.00"), "เครื่อง", null)),
				null));

		verify(services).save(any(ServiceItem.class));
	}

	@Test
	void reorderWritesSequentialSortOrder() {
		ServiceItemRepository services = mock(ServiceItemRepository.class);
		ServiceOptionRepository options = mock(ServiceOptionRepository.class);
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCatalogService catalog = new AdminCatalogService(services, options, categories, users);

		Category category = new Category();
		category.setCategoryId(1);
		category.setName("บริการทั่วไป");
		ServiceItem first = new ServiceItem();
		first.setId(1L);
		first.setName("ล้างแอร์");
		first.setSortOrder(1);
		first.setCategory(category);
		ServiceItem second = new ServiceItem();
		second.setId(2L);
		second.setName("ติดตั้งแอร์");
		second.setSortOrder(2);
		second.setCategory(category);
		when(services.findAllByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(first, second));
		when(options.findByService_IdOrderBySortOrderAscIdAsc(any())).thenReturn(List.of());

		catalog.reorder(List.of(2L, 1L));

		assertEquals(1, second.getSortOrder());
		assertEquals(2, first.getSortOrder());
		verify(services).saveAll(List.of(first, second));
	}
}
