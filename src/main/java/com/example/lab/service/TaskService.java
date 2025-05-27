package com.example.lab.service;

import com.example.lab.model.Task;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {
    List<Task> getAllTasksByUserId(String userId);
    List<Task> getPendingTasksByUserId(String userId);
    List<Task> getOverdueTasks(LocalDateTime now);
    Task createTask(Task task);
    void deleteTask(String id);
}
