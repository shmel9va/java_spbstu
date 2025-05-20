package com.example.lab.repository;

import com.example.lab.model.Notification;
import java.util.List;


public interface NotificationRepository {
    List<Notification> findByUserId(String userId);
    List<Notification> findPendingByUserId(String userId);
    void markAsRead(String id);
    Notification save(Notification notification);
}
