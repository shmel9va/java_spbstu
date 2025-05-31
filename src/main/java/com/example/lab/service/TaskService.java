package com.example.lab.service;

import com.example.lab.model.Task;
import java.util.List;

public interface TaskService {
    List<Task> getAllTasksByUserId(String userId);
    List<Task> getPendingTasksByUserId(String userId);
    Task createTask(Task task);
    Task deleteTask(String id);
    Task getTaskById(String id);
}
