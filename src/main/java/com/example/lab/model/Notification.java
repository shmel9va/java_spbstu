package com.example.lab.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {
    private String id;
    private String userId;
    private String message;
    private LocalDateTime creationDate;
    private boolean read;

    public Notification() {
        this.id = UUID.randomUUID().toString();
        this.creationDate = LocalDateTime.now();
        this.read = false;
    }

    public Notification(String userId, String message) {
        this();
        this.userId = userId;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
