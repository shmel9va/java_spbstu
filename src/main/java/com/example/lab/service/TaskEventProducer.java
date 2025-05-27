package com.example.lab.service;

import com.example.lab.model.TaskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;

    @Value("${app.kafka.task-events-topic:task-events}")
    private String taskEventsTopic;

    @Autowired
    public TaskEventProducer(KafkaTemplate<String, TaskEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTaskEvent(TaskEvent taskEvent) {
        log.info("Sending task event: {}", taskEvent);
        kafkaTemplate.send(taskEventsTopic, taskEvent.getTaskId(), taskEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Task event sent successfully: {}", taskEvent);
                    } else {
                        log.error("Failed to send task event: {}", taskEvent, ex);
                    }
                });
    }
} 