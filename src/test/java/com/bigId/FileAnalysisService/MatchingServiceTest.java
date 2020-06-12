package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.pipeline.MatcherService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MatchingServiceTest {


	@Test
	void contextLoads() {
		assertThat(1).isEqualTo(5-4);
	}

}
