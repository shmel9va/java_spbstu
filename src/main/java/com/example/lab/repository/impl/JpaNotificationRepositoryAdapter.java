package com.example.lab.repository.impl;

import com.example.lab.model.Notification;
import com.example.lab.repository.NotificationRepository;
import com.example.lab.repository.jpa.JpaNotificationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("database")
public class JpaNotificationRepositoryAdapter implements NotificationRepository {
    
    private final JpaNotificationRepository jpaNotificationRepository;
    
    public JpaNotificationRepositoryAdapter(JpaNotificationRepository jpaNotificationRepository) {
        this.jpaNotificationRepository = jpaNotificationRepository;
    }
    
    @Override
    public List<Notification> findByUserId(String userId) {
        return jpaNotificationRepository.findByUserId(userId);
    }
    
    @Override
    public List<Notification> findPendingByUserId(String userId) {
        return jpaNotificationRepository.findPendingByUserId(userId);
    }
    
    @Override
    public Notification save(Notification notification) {
        return jpaNotificationRepository.save(notification);
    }
    
    @Override
    public void markAsRead(String id) {
        jpaNotificationRepository.markAsRead(id);
    }
}
