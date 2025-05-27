package com.example.lab.kafka;

import com.example.lab.model.TaskEvent;
import com.example.lab.service.TaskEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class KafkaIntegrationTest {

    @Autowired
    private TaskEventProducer taskEventProducer;

    @Test
    public void taskEventProducer_SendEvent_NoException() {
        // Создаем тестовое событие
        TaskEvent taskEvent = new TaskEvent(
                "test-task-id",
                "test-user-id", 
                "CREATED",
                "Test task created!"
        );

        // Проверяем что producer не выбрасывает исключения
        assertDoesNotThrow(() -> {
            taskEventProducer.sendTaskEvent(taskEvent);
        });
    }

    @Test
    public void taskEvent_Creation_ValidData() {
        TaskEvent taskEvent = new TaskEvent(
                "task-123",
                "user-456",
                "DELETED",
                "Task was deleted"
        );

        assertEquals("task-123", taskEvent.getTaskId());
        assertEquals("user-456", taskEvent.getUserId());
        assertEquals("DELETED", taskEvent.getEventType());
        assertEquals("Task was deleted", taskEvent.getMessage());
        assertTrue(taskEvent.getTimestamp() > 0);
    }
} 