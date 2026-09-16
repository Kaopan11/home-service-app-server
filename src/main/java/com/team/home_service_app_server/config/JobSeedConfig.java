package com.team.home_service_app_server.config;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.team.home_service_app_server.entity.JobStatus;
import com.team.home_service_app_server.entity.ServiceItem;
import com.team.home_service_app_server.entity.ServiceJob;
import com.team.home_service_app_server.entity.User;
import com.team.home_service_app_server.entity.UserRole;
import com.team.home_service_app_server.repository.ServiceItemRepository;
import com.team.home_service_app_server.repository.ServiceJobRepository;
import com.team.home_service_app_server.repository.UserRepository;

@Configuration
public class JobSeedConfig {

	private static final String SEED_CUSTOMER_EMAIL = "seed-customer@homeservices.local";

	@Bean
	@Order(2)
	CommandLineRunner seedWaitingJobs(
			UserRepository users,
			ServiceItemRepository services,
			ServiceJobRepository jobs) {
		return args -> {
			if (jobs.countByStatus(JobStatus.WAITING_ACCEPT) > 0) {
				return;
			}

			List<ServiceItem> catalog = services.findAllByOrderBySortOrderAsc();
			if (catalog.size() < 3) {
				return;
			}

			User customer = users.findByEmail(SEED_CUSTOMER_EMAIL).orElseGet(() -> {
				Instant now = Instant.now();
				User seed = new User();
				seed.setPublicId(UUID.randomUUID());
				seed.setUsername(SEED_CUSTOMER_EMAIL);
				seed.setEmail(SEED_CUSTOMER_EMAIL);
				seed.setFullName("คุณลูกค้า ตัวอย่าง");
				seed.setFirstName("คุณลูกค้า");
				seed.setLastName("ตัวอย่าง");
				seed.setPhone("0810000000");
				seed.setRole(UserRole.USER);
				seed.setCreatedAt(now);
				seed.setUpdatedAt(now);
				return users.save(seed);
			});

			jobs.saveAll(List.of(
					job(customer, catalog.get(0), "332 อาคารเออเนียทาวเวอร์ เขตห้วยขวาง กรุงเทพฯ"),
					job(customer, catalog.get(1), "88 ถนนลาดพร้าว เขตจตุจักร กรุงเทพฯ"),
					job(customer, catalog.get(2), "12 ซอยสุขุมวิท 21 เขตคลองเตย กรุงเทพฯ")));
		};
	}

	private static ServiceJob job(User customer, ServiceItem service, String address) {
		ServiceJob item = new ServiceJob();
		item.setCustomer(customer);
		item.setService(service);
		item.setAddress(address);
		item.setStatus(JobStatus.WAITING_ACCEPT);
		return item;
	}
}
