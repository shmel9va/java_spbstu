package com.example.lab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {
    private String taskId;
    private String userId;
    private String eventType; // "CREATED", "DELETED", "UPDATED"
    private String message;
    private long timestamp;
    
    public TaskEvent(String taskId, String userId, String eventType, String message) {
        this.taskId = taskId;
        this.userId = userId;
        this.eventType = eventType;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
} 