package com.example.lab.service.impl;

import com.example.lab.model.Notification;
import com.example.lab.model.Task;
import com.example.lab.service.NotificationService;
import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    private final TaskService taskService;
    private final NotificationService notificationService;

    @Autowired
    public SchedulerServiceImpl(TaskService taskService, 
                               NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    @Override
    @Scheduled(fixedRate = 60000) // минута
    public void checkOverdueTasks() {
        System.out.println("запуск планировщика: проверка просроченных задач в " + LocalDateTime.now());
        
        LocalDateTime currentTime = LocalDateTime.now();
        List<Task> overdueTasks = taskService.findOverdueTasks(currentTime);
        
        if (!overdueTasks.isEmpty()) {
            System.out.println("найдено просроченных задач: " + overdueTasks.size());
            
            for (Task task : overdueTasks) {
                createOverdueNotificationAsync(task);
            }
        } else {
            System.out.println("просроченных задач не найдено");
        }
    }

    @Override
    @Async
    public void createOverdueNotificationAsync(Task task) {
        System.out.println("асинхронное создание уведомления для просроченной задачи: " + task.getId());
        
        String message = String.format("задача '%s' просрочена! целевая дата: %s", 
                                      task.getTitle(), 
                                      task.getTargetDate());
        
        Notification notification = new Notification();
        notification.setUserId(task.getUserId());
        notification.setTaskId(task.getId());
        notification.setMessage(message);
        notification.setRead(false);
        
        notificationService.createNotification(notification);
        
        System.out.println("уведомление о просроченной задаче создано для пользователя: " + task.getUserId());
    }

    @Override
    @Async
    public void markTasksAsCompletedAsync(List<String> taskIds) {
        System.out.println("асинхронная пометка задач как завершенных: " + taskIds.size() + " задач");
        
        for (String taskId : taskIds) {
            try {
                Task completedTask = taskService.markTaskAsCompleted(taskId);
                if (completedTask != null) {
                    System.out.println("задача помечена как завершенная: " + taskId);
                } else {
                    System.out.println("не удалось найти задачу для завершения: " + taskId);
                }
            } catch (Exception e) {
                System.err.println("ошибка при пометке задачи как завершенной: " + taskId + ", ошибка: " + e.getMessage());
            }
        }
        
        System.out.println("асинхронная пометка задач завершена");
    }
} 