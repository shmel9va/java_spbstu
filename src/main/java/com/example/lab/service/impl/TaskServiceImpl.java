package com.example.lab.service.impl;

import com.example.lab.model.Task;
import com.example.lab.model.TaskEvent;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.TaskService;
import com.example.lab.service.TaskEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskEventProducer taskEventProducer;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskEventProducer taskEventProducer) {
        this.taskRepository = taskRepository;
        this.taskEventProducer = taskEventProducer;
    }

    @Override
    @Cacheable(value = "userTasks", key = "#userId")
    public List<Task> getAllTasksByUserId(String userId) {
        return taskRepository.findByUserId(userId);
    }

    @Override
    @Cacheable(value = "pendingTasks", key = "#userId")
    public List<Task> getPendingTasksByUserId(String userId) {
        return taskRepository.findPendingByUserId(userId);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "userTasks", key = "#task.userId"),
        @CacheEvict(value = "pendingTasks", key = "#task.userId")
    })
    public Task createTask(Task task) {
        Task savedTask = taskRepository.save(task);

        // Отправляем событие в Kafka вместо прямого создания уведомления
        TaskEvent taskEvent = new TaskEvent(
                savedTask.getId(),
                savedTask.getUserId(),
                "CREATED",
                "Task created!"
        );
        taskEventProducer.sendTaskEvent(taskEvent);

        return savedTask;
    }

    @Override
    public void deleteTask(String id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            String userId = task.getUserId();
            task.setDeleted(true);
            taskRepository.save(task);

            // Очищаем кэши вручную после операции
            evictTaskCaches(userId, id);

            // Отправляем событие в Kafka вместо прямого создания уведомления
            TaskEvent taskEvent = new TaskEvent(
                    task.getId(),
                    task.getUserId(),
                    "DELETED",
                    "Task deleted!"
            );
            taskEventProducer.sendTaskEvent(taskEvent);
        }
    }

    @Caching(evict = {
        @CacheEvict(value = "userTasks", key = "#userId"),
        @CacheEvict(value = "pendingTasks", key = "#userId"),
        @CacheEvict(value = "tasks", key = "#taskId")
    })
    private void evictTaskCaches(String userId, String taskId) {
        // Метод для очистки кэшей
    }
}
