package com.smooth.pothole_analysis_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class PotholeAnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PotholeAnalysisServiceApplication.class, args);
	}

}
