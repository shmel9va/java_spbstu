package com.example.lab.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private String id;
    private String userId;
    private String title;
    private String description;
    private boolean completed;
    private boolean deleted;
    private LocalDateTime creationDate;
    private LocalDateTime targetDate;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.creationDate = LocalDateTime.now();
        this.completed = false;
        this.deleted = false;
    }

    public Task(String userId, String title, String description, LocalDateTime targetDate) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetDate = targetDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }
}
