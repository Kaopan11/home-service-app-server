package com.team.home_service_app_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HomeServiceAppServerApplication {

	public static void main(String[] args) {
		com.team.home_service_app_server.config.DotenvLoader.copyIntoSystemProperties(
				com.team.home_service_app_server.config.DotenvLoader.read());
		SpringApplication.run(HomeServiceAppServerApplication.class, args);
	}

}
