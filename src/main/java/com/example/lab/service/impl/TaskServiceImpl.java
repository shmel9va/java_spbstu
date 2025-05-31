package com.example.lab.service.impl;

import com.example.lab.model.Task;
import com.example.lab.model.Notification;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.TaskService;
import com.example.lab.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Cacheable(value = "tasks", key = "'user_' + #userId")
    public List<Task> getAllTasksByUserId(String userId) {
        System.out.println("ПОПАДАНИЕ В БД! getAllTasksByUserId для userId: " + userId);
        return taskRepository.findByUserId(userId);
    }

    @Override
    @Cacheable(value = "tasks", key = "'user_pending_' + #userId")
    public List<Task> getPendingTasksByUserId(String userId) {
        System.out.println("ПОПАДАНИЕ В БД! getPendingTasksByUserId для userId: " + userId);
        return taskRepository.findPendingByUserId(userId);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "tasks", key = "'user_' + #task.userId"),
        @CacheEvict(value = "tasks", key = "'user_pending_' + #task.userId")
    })
    public Task createTask(Task task) {
        System.out.println("ОЧИСТКА КЭША при создании задачи для userId: " + task.getUserId());
        Task savedTask = taskRepository.save(task);

        notificationService.createNotification(
                new Notification(savedTask.getUserId(), savedTask.getId(), "Task created!")
        );


        return savedTask;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "tasks", key = "'task_' + #id"),
        @CacheEvict(value = "tasks", key = "'user_' + #result.userId", condition = "#result != null"),
        @CacheEvict(value = "tasks", key = "'user_pending_' + #result.userId", condition = "#result != null")
    })
    public Task deleteTask(String id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            System.out.println("ОЧИСТКА КЭША при удалении задачи для userId: " + task.getUserId());
            task.setDeleted(true);
            Task savedTask = taskRepository.save(task);

            notificationService.createNotification(
                    new Notification(task.getUserId(), task.getId(), "Task deleted!")
            );
            
            return savedTask;
        }
        return null;
    }

    @Override
    @Cacheable(value = "tasks", key = "'task_' + #id")
    public Task getTaskById(String id) {
        System.out.println("ПОПАДАНИЕ В БД! getTaskById для taskId: " + id);
        return taskRepository.findById(id).orElse(null);
    }
}
