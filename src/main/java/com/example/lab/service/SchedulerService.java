package com.example.lab.service;

public interface SchedulerService {
    void checkOverdueTasks();
    void processOverdueTasksAsync();
} 