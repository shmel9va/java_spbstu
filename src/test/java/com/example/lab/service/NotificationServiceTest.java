package com.example.lab.service;

import com.example.lab.model.Notification;
import com.example.lab.repository.NotificationRepository;
import com.example.lab.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private final String userId = "user123";

    @BeforeEach
    public void setUp() {
        testNotification = new Notification(userId, "task123", "Тестовое уведомление");
        testNotification.setId("notification1");
        testNotification.setRead(false);
    }

    @Test
    public void getAllNotificationsByUserId_NotificationsExist_ReturnsNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserId(userId)).thenReturn(notifications);

        List<Notification> result = notificationService.getAllNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Тестовое уведомление", result.get(0).getMessage());
        verify(notificationRepository).findByUserId(userId);
    }

    @Test
    public void getPendingNotificationsByUserId_PendingNotificationsExist_ReturnsPendingNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findPendingByUserId(userId)).thenReturn(notifications);

        List<Notification> result = notificationService.getPendingNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Тестовое уведомление", result.get(0).getMessage());
        assertEquals(false, result.get(0).isRead());
        verify(notificationRepository).findPendingByUserId(userId);
    }

    @Test
    public void markNotificationAsRead_NotificationExists_MarksAsRead() {
        String notificationId = "notification1";
        doNothing().when(notificationRepository).markAsRead(notificationId);

        notificationService.markNotificationAsRead(notificationId);

        verify(notificationRepository).markAsRead(notificationId);
    }

    @Test
    public void createNotification_ValidData_ReturnsCreatedNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification input = new Notification(userId, "task123", "Тестовое уведомление"); // 👈 taskId добавлен
        Notification result = notificationService.createNotification(input);

        assertNotNull(result);
        assertEquals("Тестовое уведомление", result.getMessage());
        assertEquals(userId, result.getUserId());
        verify(notificationRepository).save(any(Notification.class));
    }
}
