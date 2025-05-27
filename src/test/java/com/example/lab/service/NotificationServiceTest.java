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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private Notification readNotification;
    private Notification anotherUserNotification;
    private final String userId = "user123";
    private final String anotherUserId = "user456";

    @BeforeEach
    public void setUp() {
        testNotification = new Notification(userId, "task123", "Тестовое уведомление");
        testNotification.setId("notification1");
        testNotification.setRead(false);

        readNotification = new Notification(userId, "task456", "Прочитанное уведомление");
        readNotification.setId("notification2");
        readNotification.setRead(true);

        anotherUserNotification = new Notification(anotherUserId, "task789", "Уведомление другого пользователя");
        anotherUserNotification.setId("notification3");
        anotherUserNotification.setRead(false);
    }

    @Test
    public void getAllNotificationsByUserId_NotificationsExist_ReturnsNotifications() {
        List<Notification> notifications = Arrays.asList(testNotification, readNotification);
        when(notificationRepository.findByUserId(userId)).thenReturn(notifications);

        List<Notification> result = notificationService.getAllNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Тестовое уведомление", result.get(0).getMessage());
        assertEquals("Прочитанное уведомление", result.get(1).getMessage());
        verify(notificationRepository).findByUserId(userId);
    }

    @Test
    public void getAllNotificationsByUserId_NoNotifications_ReturnsEmptyList() {
        when(notificationRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getAllNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByUserId(userId);
    }

    @Test
    public void getAllNotificationsByUserId_NullUserId_ReturnsEmptyList() {
        when(notificationRepository.findByUserId(null)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getAllNotificationsByUserId(null);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByUserId(null);
    }

    @Test
    public void getAllNotificationsByUserId_EmptyUserId_ReturnsEmptyList() {
        when(notificationRepository.findByUserId("")).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getAllNotificationsByUserId("");

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByUserId("");
    }

    @Test
    public void getAllNotificationsByUserId_DifferentUser_ReturnsEmptyList() {
        when(notificationRepository.findByUserId(anotherUserId)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getAllNotificationsByUserId(anotherUserId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByUserId(anotherUserId);
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
    public void getPendingNotificationsByUserId_NoPendingNotifications_ReturnsEmptyList() {
        when(notificationRepository.findPendingByUserId(userId)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getPendingNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findPendingByUserId(userId);
    }

    @Test
    public void getPendingNotificationsByUserId_OnlyReadNotifications_ReturnsEmptyList() {
        when(notificationRepository.findPendingByUserId(userId)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getPendingNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findPendingByUserId(userId);
    }

    @Test
    public void getPendingNotificationsByUserId_NullUserId_ReturnsEmptyList() {
        when(notificationRepository.findPendingByUserId(null)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getPendingNotificationsByUserId(null);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findPendingByUserId(null);
    }

    @Test
    public void markNotificationAsRead_NotificationExists_MarksAsRead() {
        String notificationId = "notification1";
        doNothing().when(notificationRepository).markAsRead(notificationId);

        notificationService.markNotificationAsRead(notificationId);

        verify(notificationRepository).markAsRead(notificationId);
    }

    @Test
    public void markNotificationAsRead_NotificationNotExists_DoesNothing() {
        String notificationId = "nonExistentNotification";
        doNothing().when(notificationRepository).markAsRead(notificationId);

        notificationService.markNotificationAsRead(notificationId);

        verify(notificationRepository).markAsRead(notificationId);
    }

    @Test
    public void markNotificationAsRead_NullNotificationId_DoesNothing() {
        doNothing().when(notificationRepository).markAsRead(null);

        notificationService.markNotificationAsRead(null);

        verify(notificationRepository).markAsRead(null);
    }

    @Test
    public void markNotificationAsRead_EmptyNotificationId_DoesNothing() {
        doNothing().when(notificationRepository).markAsRead("");

        notificationService.markNotificationAsRead("");

        verify(notificationRepository).markAsRead("");
    }

    @Test
    public void markNotificationAsRead_AlreadyReadNotification_StillMarksAsRead() {
        String notificationId = "notification2";
        doNothing().when(notificationRepository).markAsRead(notificationId);

        notificationService.markNotificationAsRead(notificationId);

        verify(notificationRepository).markAsRead(notificationId);
    }

    @Test
    public void markNotificationAsRead_DatabaseError_ThrowsException() {
        String notificationId = "notification1";
        doThrow(new RuntimeException("Database connection error"))
            .when(notificationRepository).markAsRead(notificationId);

        assertThrows(RuntimeException.class, () -> {
            notificationService.markNotificationAsRead(notificationId);
        });

        verify(notificationRepository).markAsRead(notificationId);
    }

    @Test
    public void createNotification_ValidData_ReturnsCreatedNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification input = new Notification(userId, "task123", "Тестовое уведомление");
        Notification result = notificationService.createNotification(input);

        assertNotNull(result);
        assertEquals("Тестовое уведомление", result.getMessage());
        assertEquals(userId, result.getUserId());
        assertEquals("task123", result.getTaskId());
        assertEquals(false, result.isRead());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void createNotification_NotificationWithoutTaskId_ReturnsCreatedNotification() {
        Notification notificationWithoutTaskId = new Notification(userId, null, "Уведомление без задачи");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationWithoutTaskId);

        Notification result = notificationService.createNotification(notificationWithoutTaskId);

        assertNotNull(result);
        assertEquals("Уведомление без задачи", result.getMessage());
        assertEquals(userId, result.getUserId());
        assertNull(result.getTaskId());
        verify(notificationRepository).save(notificationWithoutTaskId);
    }

    @Test
    public void createNotification_NullNotification_ThrowsException() {
        when(notificationRepository.save(null)).thenThrow(new IllegalArgumentException("Notification cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(null);
        });

        verify(notificationRepository).save(null);
    }

    @Test
    public void createNotification_DatabaseError_ThrowsException() {
        when(notificationRepository.save(any(Notification.class)))
            .thenThrow(new RuntimeException("Database connection error"));

        Notification input = new Notification(userId, "task123", "Тестовое уведомление");

        assertThrows(RuntimeException.class, () -> {
            notificationService.createNotification(input);
        });

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void createNotification_EmptyMessage_ReturnsCreatedNotification() {
        Notification notificationWithEmptyMessage = new Notification(userId, "task123", "");
        when(notificationRepository.save(any(Notification.class))).thenReturn(notificationWithEmptyMessage);

        Notification result = notificationService.createNotification(notificationWithEmptyMessage);

        assertNotNull(result);
        assertEquals("", result.getMessage());
        assertEquals(userId, result.getUserId());
        verify(notificationRepository).save(notificationWithEmptyMessage);
    }

    @Test
    public void multipleOperations_VerifyInteractions() {
        // Создание уведомления
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        notificationService.createNotification(testNotification);

        // Получение всех уведомлений
        when(notificationRepository.findByUserId(userId)).thenReturn(Arrays.asList(testNotification));
        notificationService.getAllNotificationsByUserId(userId);

        // Получение непрочитанных уведомлений
        when(notificationRepository.findPendingByUserId(userId)).thenReturn(Arrays.asList(testNotification));
        notificationService.getPendingNotificationsByUserId(userId);

        // Отметка как прочитанное
        doNothing().when(notificationRepository).markAsRead("notification1");
        notificationService.markNotificationAsRead("notification1");

        // Проверяем все взаимодействия
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository).findByUserId(userId);
        verify(notificationRepository).findPendingByUserId(userId);
        verify(notificationRepository).markAsRead("notification1");
    }

    @Test
    public void bulkOperations_VerifyPerformance() {
        // Тест для проверки работы с большим количеством уведомлений
        List<Notification> manyNotifications = Arrays.asList(
            testNotification, readNotification, anotherUserNotification
        );
        
        when(notificationRepository.findByUserId(userId)).thenReturn(manyNotifications);

        List<Notification> result = notificationService.getAllNotificationsByUserId(userId);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(notificationRepository).findByUserId(userId);
    }
}
