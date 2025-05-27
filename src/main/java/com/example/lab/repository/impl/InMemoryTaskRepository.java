package com.example.lab.repository.impl;

import com.example.lab.model.Task;
import com.example.lab.repository.TaskRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("dev")
public class InMemoryTaskRepository implements TaskRepository {
    
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public List<Task> findByUserId(String userId) {
        return tasks.values().stream()
                .filter(task -> task.getUserId().equals(userId) && !task.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findPendingByUserId(String userId) {
        return tasks.values().stream()
                .filter(task -> task.getUserId().equals(userId) && !task.isCompleted() && !task.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findOverdueTasks(LocalDateTime now) {
        return tasks.values().stream()
                .filter(task -> !task.isCompleted() && !task.isDeleted() && 
                        task.getTargetDate() != null && task.getTargetDate().isBefore(now))
                .collect(Collectors.toList());
    }

    @Override
    public Task save(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteById(String id) {
        Task task = tasks.get(id);
        if (task != null) {
            task.setDeleted(true);
            tasks.put(id, task);
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(tasks.get(id));
    }
}
