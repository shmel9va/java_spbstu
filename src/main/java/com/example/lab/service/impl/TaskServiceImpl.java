package com.example.lab.service.impl;

import com.example.lab.model.Task;
import com.example.lab.model.Notification;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.TaskService;
import com.example.lab.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Task> getAllTasksByUserId(String userId) {
        return taskRepository.findByUserId(userId);
    }

    @Override
    public List<Task> getPendingTasksByUserId(String userId) {
        return taskRepository.findPendingByUserId(userId);
    }

    @Override
    public Task createTask(Task task) {
        Task savedTask = taskRepository.save(task);

        notificationService.createNotification(
                new Notification(savedTask.getUserId(), savedTask.getId(), "Task created!")
        );


        return savedTask;
    }

    @Override
    public void deleteTask(String id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            task.setDeleted(true);
            taskRepository.save(task);

            notificationService.createNotification(
                    new Notification(task.getUserId(), task.getId(), "Task deleted!")
            );
        }
    }
}
