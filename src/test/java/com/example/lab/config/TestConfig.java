package com.example.lab.config;

import com.example.lab.service.TaskEventProducer;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public TaskEventProducer taskEventProducer() {
        return Mockito.mock(TaskEventProducer.class);
    }
} 