package com.example.lab.repository.jpa;

import com.example.lab.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JpaNotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserId(String userId);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.read = false")
    List<Notification> findPendingByUserId(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markAsRead(@Param("id") String id);
}
