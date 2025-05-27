package com.example.lab;

import com.example.lab.config.TestCacheConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestCacheConfig.class)
class LabApplicationTests {

	@Test
	void contextLoads() {
	}

}
