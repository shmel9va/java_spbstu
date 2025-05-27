package com.example.lab;

import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("dev")
public class SchedulerIntegrationTest {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskService taskService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() {
        assertNotNull(schedulerService);
        assertNotNull(taskService);
    }

    @Test
    void schedulerServiceWorks() {
        // Проверяем что можем получить просроченные задачи
        var overdueTasks = taskService.getOverdueTasks(LocalDateTime.now());
        assertNotNull(overdueTasks);
    }

    @Test
    void asyncProcessingWorks() {
        // Проверяем что асинхронная обработка не падает
        schedulerService.processOverdueTasksAsync();
    }
} 