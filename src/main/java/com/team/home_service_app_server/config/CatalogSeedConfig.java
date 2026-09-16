package com.team.home_service_app_server.config;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.team.home_service_app_server.entity.Category;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.repository.CategoryRepository;
import com.team.home_service_app_server.repository.ServiceItemRepository;

@Configuration
public class CatalogSeedConfig {

	@Bean
	CommandLineRunner seedCatalog(CategoryRepository categories, ServiceItemRepository services) {
		return args -> {
			if (services.count() > 0) {
				return;
			}

			Category general = category("บริการทั่วไป", "general");
			Category kitchen = category("บริการห้องครัว", "kitchen");
			Category bathroom = category("บริการห้องน้ำ", "bathroom");
			categories.saveAll(List.of(general, kitchen, bathroom));

			Instant stamp = Instant.parse("2022-02-12T22:30:00Z");
			services.saveAll(List.of(
					item(1, "ล้างแอร์", general, stamp),
					item(2, "ติดตั้งแอร์", general, stamp),
					item(3, "ทำความสะอาดทั่วไป", general, stamp),
					item(4, "ซ่อมแอร์", general, stamp),
					item(5, "ซ่อมเครื่องซักผ้า", general, stamp),
					item(6, "ติดตั้งเตาแก๊ส", kitchen, stamp),
					item(7, "ติดตั้งเครื่องดูดควัน", kitchen, stamp),
					item(8, "ติดตั้งชักโครก", bathroom, stamp),
					item(9, "ติดตั้งเครื่องทำน้ำอุ่น", bathroom, stamp)));
		};
	}

	private static Category category(String name, String tone) {
		Category category = new Category();
		category.setName(name);
		category.setTone(tone);
		return category;
	}

	private static ServiceItem item(int order, String name, Category category, Instant stamp) {
		ServiceItem service = new ServiceItem();
		service.setSortOrder(order);
		service.setName(name);
		service.setCategory(category);
		service.setCreatedAt(stamp);
		service.setUpdatedAt(stamp);
		return service;
	}
}
