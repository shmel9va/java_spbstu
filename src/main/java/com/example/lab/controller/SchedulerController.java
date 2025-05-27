package com.example.lab.controller;

import com.example.lab.model.Task;
import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final TaskService taskService;

    @Autowired
    public SchedulerController(SchedulerService schedulerService, TaskService taskService) {
        this.schedulerService = schedulerService;
        this.taskService = taskService;
    }

    @PostMapping("/check-overdue")
    public ResponseEntity<String> checkOverdueTasks() {
        schedulerService.checkOverdueTasks();
        return ResponseEntity.ok("Overdue tasks check initiated");
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskService.getOverdueTasks(now);
        return ResponseEntity.ok(overdueTasks);
    }

    @PostMapping("/process-overdue-async")
    public ResponseEntity<String> processOverdueTasksAsync() {
        schedulerService.processOverdueTasksAsync();
        return ResponseEntity.ok("Async processing of overdue tasks initiated");
    }
} 