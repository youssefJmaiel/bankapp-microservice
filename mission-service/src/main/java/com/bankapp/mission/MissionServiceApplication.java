package com.bankapp.mission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.bankapp.mission.client")
public class MissionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MissionServiceApplication.class, args);
	}
}