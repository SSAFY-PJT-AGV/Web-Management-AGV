package com.example.ssafy_pjt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SsafyPjtApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsafyPjtApplication.class, args);
	}
}
