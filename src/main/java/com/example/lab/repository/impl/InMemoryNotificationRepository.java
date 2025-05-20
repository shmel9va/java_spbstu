package com.example.lab.repository.impl;

import com.example.lab.model.Notification;
import com.example.lab.repository.NotificationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("dev")
public class InMemoryNotificationRepository implements NotificationRepository {
    
    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();

    @Override
    public List<Notification> findByUserId(String userId) {
        return notifications.values().stream()
                .filter(notification -> notification.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findPendingByUserId(String userId) {
        return notifications.values().stream()
                .filter(notification -> notification.getUserId().equals(userId) && !notification.isRead())
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(String id) {
        Notification notification = notifications.get(id);
        if (notification != null) {
            notification.setRead(true);
            notifications.put(id, notification);
        }
    }
    
    @Override
    public Notification save(Notification notification) {
        notifications.put(notification.getId(), notification);
        return notification;
    }
}
