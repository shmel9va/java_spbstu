package com.example.lab.service;

import com.example.lab.model.Task;
import com.example.lab.repository.TaskRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskEventProducer taskEventProducer;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    private Task overdueTask;
    private Task normalTask;

    @BeforeEach
    void setUp() {
        overdueTask = new Task();
        overdueTask.setId("overdue-task-1");
        overdueTask.setUserId("user1");
        overdueTask.setTitle("Overdue Task");
        overdueTask.setDescription("This task is overdue");
        overdueTask.setTargetDate(LocalDateTime.now().minusDays(1));
        overdueTask.setCompleted(false);
        overdueTask.setDeleted(false);

        normalTask = new Task();
        normalTask.setId("normal-task-1");
        normalTask.setUserId("user1");
        normalTask.setTitle("Normal Task");
        normalTask.setDescription("This task is not overdue");
        normalTask.setTargetDate(LocalDateTime.now().plusDays(1));
        normalTask.setCompleted(false);
        normalTask.setDeleted(false);
    }

    @Test
    void processOverdueTasksAsync_WithOverdueTasks_SendsNotifications() {
        // Arrange
        List<Task> overdueTasks = Arrays.asList(overdueTask);
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(overdueTasks);

        // Act
        schedulerService.processOverdueTasksAsync();

        // Assert
        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class));
        verify(taskEventProducer).sendTaskEvent(any());
    }

    @Test
    void processOverdueTasksAsync_WithNoOverdueTasks_DoesNotSendNotifications() {
        // Arrange
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // Act
        schedulerService.processOverdueTasksAsync();

        // Assert
        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class));
        verify(taskEventProducer, never()).sendTaskEvent(any());
    }

    @Test
    void checkOverdueTasks_CallsProcessOverdueTasksAsync() {
        // Arrange
        when(taskRepository.findOverdueTasks(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // Act
        schedulerService.checkOverdueTasks();

        // Assert
        verify(taskRepository).findOverdueTasks(any(LocalDateTime.class));
    }
} 