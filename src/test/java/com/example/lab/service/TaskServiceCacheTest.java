package com.example.lab.service;

import com.example.lab.model.Task;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceCacheTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private CacheManager cacheManager;
    private Task testTask;
    private final String userId = "user123";

    @BeforeEach
    public void setUp() {
        cacheManager = new ConcurrentMapCacheManager("userTasks", "pendingTasks", "tasks");
        
        testTask = new Task();
        testTask.setId("task1");
        testTask.setUserId(userId);
        testTask.setTitle("Тестовая задача");
        testTask.setCompleted(false);
        testTask.setDeleted(false);
    }

    @Test
    public void getAllTasksByUserId_FirstCall_CallsRepository() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByUserId(userId)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByUserId(userId);
    }

    @Test
    public void getPendingTasksByUserId_FirstCall_CallsRepository() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findPendingByUserId(userId)).thenReturn(tasks);

        List<Task> result = taskService.getPendingTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findPendingByUserId(userId);
    }

    @Test
    public void createTask_ClearsCache() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.createTask(testTask);

        verify(taskRepository, times(1)).save(testTask);
        verify(notificationService, times(1)).createNotification(any());
    }

    @Test
    public void deleteTask_ClearsCache() {
        when(taskRepository.findById("task1")).thenReturn(java.util.Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        taskService.deleteTask("task1");

        assertTrue(testTask.isDeleted());
        verify(taskRepository, times(1)).findById("task1");
        verify(taskRepository, times(1)).save(testTask);
        verify(notificationService, times(1)).createNotification(any());
    }
} 