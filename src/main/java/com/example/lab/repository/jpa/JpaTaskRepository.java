package com.example.lab.repository.jpa;

import com.example.lab.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaTaskRepository extends JpaRepository<Task, String> {
    List<Task> findByUserIdAndDeletedFalse(String userId);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.completed = false AND t.deleted = false")
    List<Task> findPendingByUserId(@Param("userId") String userId);
}
