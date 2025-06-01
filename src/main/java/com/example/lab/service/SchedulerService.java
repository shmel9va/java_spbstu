package com.example.lab.service;

import com.example.lab.model.Task;

import java.util.List;

public interface SchedulerService {
    // Периодическая проверка просроченных задач
    void checkOverdueTasks();
    
    // Асинхронное создание уведомления о просроченной задаче
    void createOverdueNotificationAsync(Task task);
    
    // Асинхронная пометка задач как завершенных
    void markTasksAsCompletedAsync(List<String> taskIds);
} 