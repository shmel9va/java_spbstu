package com.example.lab.service;

import com.example.lab.model.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> getAllNotificationsByUserId(String userId);
    List<Notification> getPendingNotificationsByUserId(String userId);
    void markNotificationAsRead(String id);
}
