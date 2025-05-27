package com.example.lab.service;

import com.example.lab.model.Notification;
import com.example.lab.model.TaskEvent;
import com.example.lab.service.impl.NotificationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskEventListener {

    private final NotificationServiceImpl notificationService;

    @Autowired
    public TaskEventListener(NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.task-events-topic:task-events}", groupId = "${spring.kafka.consumer.group-id:task-notification-group}")
    public void handleTaskEvent(TaskEvent taskEvent) {
        log.info("Received task event: {}", taskEvent);
        
        try {
            // Создаем уведомление на основе события
            Notification notification = new Notification(
                    taskEvent.getUserId(),
                    taskEvent.getTaskId(),
                    taskEvent.getMessage()
            );
            
            notificationService.createNotificationInternal(notification);
            log.info("Notification created for task event: {}", taskEvent);
            
        } catch (Exception e) {
            log.error("Failed to process task event: {}", taskEvent, e);
        }
    }
} 