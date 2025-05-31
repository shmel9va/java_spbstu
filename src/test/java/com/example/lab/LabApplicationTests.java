package com.example.lab;

import com.example.lab.kafkaEvents.TaskEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
@EnableAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
class LabApplicationTests {

	@MockitoBean
	private KafkaTemplate<String, TaskEvent> kafkaTemplate;

	@Test
	void contextLoads() {
	}

}
