package com.smooth.pothole_analysis_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PotholeAnalysisServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
