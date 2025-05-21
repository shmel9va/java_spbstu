package com.example.lab.service;

import com.example.lab.model.Task;
import com.example.lab.model.Notification;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask;
    private final String userId = "user123";

    @BeforeEach
    public void setUp() {
        testTask = new Task();
        testTask.setId("task1");
        testTask.setUserId(userId);
        testTask.setTitle("Тестовая задача");
        testTask.setDescription("Описание тестовой задачи");
        testTask.setCompleted(false);
    }

    @Test
    public void getAllTasksByUserId_TasksExist_ReturnsTasks() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByUserId(userId)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Тестовая задача", result.get(0).getTitle());
        verify(taskRepository).findByUserId(userId);
    }

    @Test
    public void getPendingTasksByUserId_PendingTasksExist_ReturnsPendingTasks() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findPendingByUserId(userId)).thenReturn(tasks);

        List<Task> result = taskService.getPendingTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Тестовая задача", result.get(0).getTitle());
        assertEquals(false, result.get(0).isCompleted());
        verify(taskRepository).findPendingByUserId(userId);
    }

    @Test
    public void createTask_ValidTask_ReturnsCreatedTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class))).thenReturn(new Notification(userId, "task1", "Task created!"));

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals("Тестовая задача", result.getTitle());
        verify(taskRepository).save(testTask);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_TaskExists_DeletesTask() {
        String taskId = "task1";
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class))).thenReturn(new Notification(userId, taskId, "Task deleted!"));

        taskService.deleteTask(taskId);

        assertEquals(true, testTask.isDeleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(testTask);
        verify(notificationService).createNotification(any(Notification.class));
    }
}
