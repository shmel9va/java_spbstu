package com.example.lab.service.impl;

import com.example.lab.model.Task;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }
}
