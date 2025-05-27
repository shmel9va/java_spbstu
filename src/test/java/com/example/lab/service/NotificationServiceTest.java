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
    public void getAllNotificationsByUserId_NotificationsExist_ReturnsAllNotifications() {
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
    public void getAllNotificationsByUserId_DifferentUser_ReturnsEmptyList() {
        when(notificationRepository.findByUserId(anotherUserId)).thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.getAllNotificationsByUserId(anotherUserId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(notificationRepository).findByUserId(anotherUserId);
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
    public void getPendingNotificationsByUserId_PendingNotificationsExist_ReturnsPendingNotifications() {
        List<Notification> pendingNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findPendingByUserId(userId)).thenReturn(pendingNotifications);

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
    public void multipleOperations_VerifyInteractions() {
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
        verify(notificationRepository).findByUserId(userId);
        verify(notificationRepository).findPendingByUserId(userId);
        verify(notificationRepository).markAsRead("notification1");
    }
}
