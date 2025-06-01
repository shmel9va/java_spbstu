package com.example.lab.repository.impl;

import com.example.lab.model.Task;
import com.example.lab.repository.TaskRepository;
import com.example.lab.repository.jpa.JpaTaskRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"database", "mysql"})
public class JpaTaskRepositoryAdapter implements TaskRepository {
    
    private final JpaTaskRepository jpaTaskRepository;
    
    public JpaTaskRepositoryAdapter(JpaTaskRepository jpaTaskRepository) {
        this.jpaTaskRepository = jpaTaskRepository;
    }
    
    @Override
    public List<Task> findByUserId(String userId) {
        return jpaTaskRepository.findByUserIdAndDeletedFalse(userId);
    }
    
    @Override
    public List<Task> findPendingByUserId(String userId) {
        return jpaTaskRepository.findPendingByUserId(userId);
    }
    
    @Override
    public Task save(Task task) {
        return jpaTaskRepository.save(task);
    }

    @Override
    public void deleteById(String id) {
        Task task = jpaTaskRepository.findById(id).orElse(null);
        if (task != null) {
            task.setDeleted(true);
            jpaTaskRepository.save(task);
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        return jpaTaskRepository.findById(id);
    }

    @Override
    public List<Task> findOverdueTasks(LocalDateTime currentTime) {
        return jpaTaskRepository.findOverdueTasks(currentTime);
    }
}
