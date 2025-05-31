package com.example.lab.kafkaEvents;

public record TaskEvent(
        TaskEventTypeEnum eventType, // CREATE, DELETE
        String taskId,
        String userId
) {} 