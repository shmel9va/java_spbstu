package com.example.lab.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.task-event}")
    private String taskCreatedTopicName;

    @Bean
    public NewTopic taskCreatedTopic() {
        return TopicBuilder.name(taskCreatedTopicName)
                .partitions(1)
                .replicas(1)
                .build();
    }
} 