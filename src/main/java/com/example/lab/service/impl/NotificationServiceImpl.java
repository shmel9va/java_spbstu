package com.example.lab.service.impl;

import com.example.lab.model.Notification;
import com.example.lab.repository.NotificationRepository;
import com.example.lab.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Внутренний метод для создания уведомлений через Kafka listener
    public Notification createNotificationInternal(Notification notification) {
        return notificationRepository.save(notification);
    }
}
