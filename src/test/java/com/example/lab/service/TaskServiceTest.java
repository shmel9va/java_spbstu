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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
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
    private Task completedTask;
    private Task deletedTask;
    private final String userId = "user123";
    private final String anotherUserId = "user456";

    @BeforeEach
    public void setUp() {
        testTask = new Task();
        testTask.setId("task1");
        testTask.setUserId(userId);
        testTask.setTitle("Тестовая задача");
        testTask.setDescription("Описание тестовой задачи");
        testTask.setCompleted(false);
        testTask.setDeleted(false);

        completedTask = new Task();
        completedTask.setId("task2");
        completedTask.setUserId(userId);
        completedTask.setTitle("Завершенная задача");
        completedTask.setDescription("Описание завершенной задачи");
        completedTask.setCompleted(true);
        completedTask.setDeleted(false);

        deletedTask = new Task();
        deletedTask.setId("task3");
        deletedTask.setUserId(userId);
        deletedTask.setTitle("Удаленная задача");
        deletedTask.setDescription("Описание удаленной задачи");
        deletedTask.setCompleted(false);
        deletedTask.setDeleted(true);
    }

    @Test
    public void getAllTasksByUserId_TasksExist_ReturnsTasks() {
        List<Task> tasks = Arrays.asList(testTask, completedTask);
        when(taskRepository.findByUserId(userId)).thenReturn(tasks);

        List<Task> result = taskService.getAllTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Тестовая задача", result.get(0).getTitle());
        assertEquals("Завершенная задача", result.get(1).getTitle());
        verify(taskRepository).findByUserId(userId);
    }

    @Test
    public void getAllTasksByUserId_NoTasks_ReturnsEmptyList() {
        when(taskRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getAllTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findByUserId(userId);
    }

    @Test
    public void getAllTasksByUserId_NullUserId_ReturnsEmptyList() {
        when(taskRepository.findByUserId(null)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getAllTasksByUserId(null);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findByUserId(null);
    }

    @Test
    public void getAllTasksByUserId_DifferentUser_ReturnsEmptyList() {
        when(taskRepository.findByUserId(anotherUserId)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getAllTasksByUserId(anotherUserId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findByUserId(anotherUserId);
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
    public void getPendingTasksByUserId_NoPendingTasks_ReturnsEmptyList() {
        when(taskRepository.findPendingByUserId(userId)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getPendingTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findPendingByUserId(userId);
    }

    @Test
    public void getPendingTasksByUserId_OnlyCompletedTasks_ReturnsEmptyList() {
        when(taskRepository.findPendingByUserId(userId)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.getPendingTasksByUserId(userId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findPendingByUserId(userId);
    }

    @Test
    public void createTask_ValidTask_ReturnsCreatedTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class)))
            .thenReturn(new Notification(userId, "task1", "Task created!"));

        Task result = taskService.createTask(testTask);

        assertNotNull(result);
        assertEquals("Тестовая задача", result.getTitle());
        assertEquals(userId, result.getUserId());
        assertEquals(false, result.isCompleted());
        verify(taskRepository).save(testTask);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void createTask_TaskWithoutDescription_ReturnsCreatedTask() {
        Task taskWithoutDescription = new Task();
        taskWithoutDescription.setUserId(userId);
        taskWithoutDescription.setTitle("Задача без описания");
        
        when(taskRepository.save(any(Task.class))).thenReturn(taskWithoutDescription);
        when(notificationService.createNotification(any(Notification.class)))
            .thenReturn(new Notification(userId, "task1", "Task created!"));

        Task result = taskService.createTask(taskWithoutDescription);

        assertNotNull(result);
        assertEquals("Задача без описания", result.getTitle());
        assertNull(result.getDescription());
        verify(taskRepository).save(taskWithoutDescription);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void createTask_NullTask_ThrowsException() {
        when(taskRepository.save(null)).thenThrow(new IllegalArgumentException("Task cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(null);
        });

        verify(taskRepository).save(null);
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    public void createTask_NotificationServiceFails_StillReturnsTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class)))
            .thenThrow(new RuntimeException("Notification service error"));

        assertThrows(RuntimeException.class, () -> {
            taskService.createTask(testTask);
        });

        verify(taskRepository).save(testTask);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_TaskExists_DeletesTask() {
        String taskId = "task1";
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class)))
            .thenReturn(new Notification(userId, taskId, "Task deleted!"));

        taskService.deleteTask(taskId);

        assertEquals(true, testTask.isDeleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(testTask);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_TaskNotExists_DoesNothing() {
        String taskId = "nonExistentTask";
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        taskService.deleteTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_AlreadyDeletedTask_StillMarksAsDeleted() {
        String taskId = "task3";
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(deletedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(deletedTask);
        when(notificationService.createNotification(any(Notification.class)))
            .thenReturn(new Notification(userId, taskId, "Task deleted!"));

        taskService.deleteTask(taskId);

        assertEquals(true, deletedTask.isDeleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(deletedTask);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_NullTaskId_DoesNothing() {
        when(taskRepository.findById(null)).thenReturn(Optional.empty());

        taskService.deleteTask(null);

        verify(taskRepository).findById(null);
        verify(taskRepository, never()).save(any(Task.class));
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_EmptyTaskId_DoesNothing() {
        when(taskRepository.findById("")).thenReturn(Optional.empty());

        taskService.deleteTask("");

        verify(taskRepository).findById("");
        verify(taskRepository, never()).save(any(Task.class));
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    public void deleteTask_NotificationServiceFails_TaskStillDeleted() {
        String taskId = "task1";
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class)))
            .thenThrow(new RuntimeException("Notification service error"));

        assertThrows(RuntimeException.class, () -> {
            taskService.deleteTask(taskId);
        });

        assertEquals(true, testTask.isDeleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(testTask);
        verify(notificationService).createNotification(any(Notification.class));
    }

    @Test
    public void multipleOperations_VerifyInteractions() {
        // Создание задачи
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(notificationService.createNotification(any(Notification.class)))
            .thenReturn(new Notification(userId, "task1", "Task created!"));

        taskService.createTask(testTask);

        // Получение задач
        when(taskRepository.findByUserId(userId)).thenReturn(Arrays.asList(testTask));
        taskService.getAllTasksByUserId(userId);

        // Удаление задачи
        when(taskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        taskService.deleteTask("task1");

        // Проверяем все взаимодействия
        verify(taskRepository, times(2)).save(any(Task.class)); // create + delete
        verify(taskRepository).findByUserId(userId);
        verify(taskRepository).findById("task1");
        verify(notificationService, times(2)).createNotification(any(Notification.class)); // create + delete
    }

    // Новые тесты для Step 8
    
    @Test
    public void findOverdueTasks_OverdueTasksExist_ReturnsOverdueTasks() {
        LocalDateTime currentTime = LocalDateTime.now();
        Task overdueTask1 = new Task(userId, "Просроченная задача 1", "Описание", currentTime.minusDays(1));
        Task overdueTask2 = new Task(userId, "Просроченная задача 2", "Описание", currentTime.minusHours(2));
        List<Task> overdueTasks = Arrays.asList(overdueTask1, overdueTask2);
        
        when(taskRepository.findOverdueTasks(currentTime)).thenReturn(overdueTasks);

        List<Task> result = taskService.findOverdueTasks(currentTime);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Просроченная задача 1", result.get(0).getTitle());
        assertEquals("Просроченная задача 2", result.get(1).getTitle());
        verify(taskRepository).findOverdueTasks(currentTime);
    }

    @Test
    public void findOverdueTasks_NoOverdueTasks_ReturnsEmptyList() {
        LocalDateTime currentTime = LocalDateTime.now();
        when(taskRepository.findOverdueTasks(currentTime)).thenReturn(Collections.emptyList());

        List<Task> result = taskService.findOverdueTasks(currentTime);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository).findOverdueTasks(currentTime);
    }

    @Test
    public void markTaskAsCompleted_TaskExists_MarksTaskAsCompleted() {
        String taskId = "task1";
        Task taskToComplete = new Task(userId, "Задача для завершения", "Описание", LocalDateTime.now().plusDays(1));
        taskToComplete.setId(taskId);
        taskToComplete.setCompleted(false);
        
        Task completedTask = new Task(userId, "Задача для завершения", "Описание", LocalDateTime.now().plusDays(1));
        completedTask.setId(taskId);
        completedTask.setCompleted(true);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskToComplete));
        when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

        Task result = taskService.markTaskAsCompleted(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(true, result.isCompleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void markTaskAsCompleted_TaskNotExists_ReturnsNull() {
        String taskId = "nonExistentTask";
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        Task result = taskService.markTaskAsCompleted(taskId);

        assertNull(result);
        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void markTaskAsCompleted_AlreadyCompletedTask_StillMarksAsCompleted() {
        String taskId = "task2";
        completedTask.setId(taskId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(completedTask));
        when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

        Task result = taskService.markTaskAsCompleted(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(true, result.isCompleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }
}
