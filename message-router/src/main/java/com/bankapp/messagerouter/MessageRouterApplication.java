package com.bankapp.messagerouter;

import com.bankapp.messagerouter.service.MqService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "com.bankapp.messagerouter")
public class MessageRouterApplication {

	public static void main(String[] args) {
		System.setProperty("java.library.path", "/opt/mqm/java/lib64"); // chemin IBM MQ
		SpringApplication.run(MessageRouterApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(MqService mqService) {
		return args -> {
			try {
				mqService.connect();
				mqService.sendMessage("Hello, this is a test message.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
}