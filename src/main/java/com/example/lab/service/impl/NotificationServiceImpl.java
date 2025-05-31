package com.example.lab.service.impl;

import com.example.lab.model.Notification;
import com.example.lab.kafkaEvents.TaskEvent;
import com.example.lab.kafkaEvents.TaskEventTypeEnum;
import com.example.lab.repository.NotificationRepository;
import com.example.lab.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> getAllNotificationsByUserId(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getPendingNotificationsByUserId(String userId) {
        return notificationRepository.findPendingByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(String id) {
        notificationRepository.markAsRead(id);
    }

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @KafkaListener(topics = "${kafka.topic.task-event}")
    @Transactional
    public void handleTaskEvent(TaskEvent taskEvent) {
        String message = switch (taskEvent.eventType()) {
            case CREATE -> "Task created!";
            case DELETE -> "Task deleted!";
        };
        Notification notification = new Notification(taskEvent.userId(), taskEvent.taskId(), message);
        createNotification(notification);
    }
}
