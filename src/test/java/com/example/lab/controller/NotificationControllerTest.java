package com.example.lab.controller;

import com.example.lab.model.Notification;
import com.example.lab.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private Notification testNotification;
    private final String userId = "user123";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        
        testNotification = new Notification(userId, "task123","Тестовое уведомление");
        testNotification.setId("notification1");
        testNotification.setRead(false);
    }

    @Test
    public void getAllUserNotifications_NotificationsExist_ReturnsNotifications() throws Exception {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationService.getAllNotificationsByUserId(anyString())).thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Тестовое уведомление"))
                .andExpect(jsonPath("$[0].userId").value(userId));
    }

    @Test
    public void getPendingUserNotifications_PendingNotificationsExist_ReturnsPendingNotifications() throws Exception {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationService.getPendingNotificationsByUserId(anyString())).thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/{userId}/pending", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Тестовое уведомление"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    public void markAsRead_NotificationExists_ReturnsNoContent() throws Exception {
        // Arrange
        String notificationId = "notification1";
        doNothing().when(notificationService).markNotificationAsRead(notificationId);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/{id}/read", notificationId))
                .andExpect(status().isNoContent());
    }
}
