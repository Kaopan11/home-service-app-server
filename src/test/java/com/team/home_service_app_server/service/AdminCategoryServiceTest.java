package com.team.home_service_app_server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.home_service_app_server.entity.Category;
import com.team.home_service_app_server.repository.CategoryRepository;

class AdminCategoryServiceTest {

	@Test
	void reorderWritesSequentialSortOrder() {
		CategoryRepository categories = mock(CategoryRepository.class);
		UserService users = mock(UserService.class);
		AdminCategoryService service = new AdminCategoryService(categories, users);

		Category first = new Category();
		first.setCategoryId(1);
		first.setName("บริการทั่วไป");
		first.setActive(true);
		first.setSortOrder(1);
		Category second = new Category();
		second.setCategoryId(2);
		second.setName("บริการห้องครัว");
		second.setActive(true);
		second.setSortOrder(2);
		when(categories.findAllByActiveTrueOrderBySortOrderAscCreatedAtAsc()).thenReturn(List.of(first, second));
		when(categories.findAllByOrderBySortOrderAscCreatedAtAsc()).thenReturn(List.of(second, first));

		service.reorder(List.of(2, 1));

		assertEquals(1, second.getSortOrder());
		assertEquals(2, first.getSortOrder());
		verify(categories).saveAll(List.of(first, second));
	}
}
