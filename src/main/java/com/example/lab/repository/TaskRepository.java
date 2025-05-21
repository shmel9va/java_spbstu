package com.example.lab.repository;

import com.example.lab.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    List<Task> findByUserId(String userId);
    List<Task> findPendingByUserId(String userId);
    Task save(Task task);
    void deleteById(String id);
    Optional<Task> findById(String id);
}
