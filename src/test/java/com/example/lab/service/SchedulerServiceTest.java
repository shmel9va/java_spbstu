package com.example.lab.service;

import com.example.lab.model.Notification;
import com.example.lab.model.Task;
import com.example.lab.service.impl.SchedulerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    private Task overdueTask;

    @BeforeEach
    void setUp() {
        overdueTask = new Task("user1", "Просроченная задача", "Описание", LocalDateTime.now().minusDays(1));
        overdueTask.setId("task1");
    }

    @Test
    void testCheckOverdueTasksWhenNoOverdueTasks() {
        // Arrange
        when(taskService.findOverdueTasks(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        schedulerService.checkOverdueTasks();

        // Assert
        verify(taskService, times(1)).findOverdueTasks(any(LocalDateTime.class));
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    void testCheckOverdueTasksWhenOverdueTasksExist() {
        // Arrange
        // Создадим вторую просроченную задачу для проверки множественных вызовов
        Task anotherOverdueTask = new Task("user1", "Еще одна просроченная задача", "Описание", LocalDateTime.now().minusHours(1));
        anotherOverdueTask.setId("task2");

        List<Task> overdueTasks = Arrays.asList(overdueTask, anotherOverdueTask); // Теперь две просроченные задачи
        when(taskService.findOverdueTasks(any(LocalDateTime.class)))
                .thenReturn(overdueTasks);

        // Act
        schedulerService.checkOverdueTasks();

        // Assert
        verify(taskService, times(1)).findOverdueTasks(any(LocalDateTime.class));
        // Проверяем, что notificationService.createNotification был вызван для КАЖДОЙ просроченной задачи
        verify(notificationService, times(overdueTasks.size())).createNotification(any(Notification.class));
    }

    @Test
    void testCreateOverdueNotificationAsync() {
        // Arrange
        Notification notification = new Notification();
        when(notificationService.createNotification(any(Notification.class)))
                .thenReturn(notification);

        // Act
        schedulerService.createOverdueNotificationAsync(overdueTask);

        // Assert
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testMarkTasksAsCompletedAsyncWithValidTasks() {
        // Arrange
        List<String> taskIds = Arrays.asList("task1", "task2");
        Task completedTask = new Task();
        completedTask.setId("task1");
        completedTask.setCompleted(true);
        
        when(taskService.markTaskAsCompleted("task1")).thenReturn(completedTask);
        when(taskService.markTaskAsCompleted("task2")).thenReturn(null);

        // Act
        schedulerService.markTasksAsCompletedAsync(taskIds);

        // Assert
        verify(taskService, times(1)).markTaskAsCompleted("task1");
        verify(taskService, times(1)).markTaskAsCompleted("task2");
    }

    @Test
    void testMarkTasksAsCompletedAsyncWithException() {
        // Arrange
        List<String> taskIds = Arrays.asList("task1");
        when(taskService.markTaskAsCompleted("task1"))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        schedulerService.markTasksAsCompletedAsync(taskIds);

        // Assert
        verify(taskService, times(1)).markTaskAsCompleted("task1");
    }

    @Test
    void testMarkTasksAsCompletedAsyncWithEmptyList() {
        // Arrange
        List<String> taskIds = Collections.emptyList();

        // Act
        schedulerService.markTasksAsCompletedAsync(taskIds);

        // Assert
        verify(taskService, never()).markTaskAsCompleted(anyString());
    }
} 