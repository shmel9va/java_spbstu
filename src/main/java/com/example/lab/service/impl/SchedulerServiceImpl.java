package com.example.lab.service.impl;

import com.example.lab.exception.ExternalServiceUnavailableException;
import com.example.lab.model.Task;
import com.example.lab.model.TaskEvent;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;

    @Autowired
    public SchedulerServiceImpl(TaskRepository taskRepository, TaskEventProducer taskEventProducer) {
        this.taskRepository = taskRepository;
        this.taskEventProducer = taskEventProducer;
    }

    @Override
    @Scheduled(fixedRate = 300000) // Каждые 5 минут
    public void checkOverdueTasks() {
        log.info("Starting scheduled check for overdue tasks");
        processOverdueTasksAsync();
    }

    @Override
    @Async
    @Transactional
    public void processOverdueTasksAsync() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Task> overdueTasks = taskRepository.findOverdueTasks(now);
            
            log.info("Found {} overdue tasks", overdueTasks.size());
            
            for (Task task : overdueTasks) {
                try {
                    TaskEvent overdueEvent = new TaskEvent(
                            task.getId(),
                            task.getUserId(),
                            "OVERDUE",
                            "Task '" + task.getTitle() + "' is overdue!"
                    );
                    
                    taskEventProducer.sendTaskEvent(overdueEvent);
                    log.info("Sent overdue notification for task: {}", task.getId());
                    
                } catch (Exception e) {
                    log.error("Failed to send overdue notification for task: {}", task.getId(), e);
                    throw new ExternalServiceUnavailableException(
                            "Failed to send overdue event to Kafka for taskId = " + task.getId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing overdue tasks", e);
        }
    }
} 