package com.example.lab.controller;

import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchedulerController.class)
public class SchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchedulerService schedulerService;

    @MockBean
    private TaskService taskService;

    @Test
    void checkOverdueTasks_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/scheduler/check-overdue"))
                .andExpect(status().isOk())
                .andExpect(content().string("Overdue tasks check initiated"));
    }

    @Test
    void getOverdueTasks_ReturnsEmptyList() throws Exception {
        when(taskService.getOverdueTasks(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/scheduler/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void processOverdueTasksAsync_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/scheduler/process-overdue-async"))
                .andExpect(status().isOk())
                .andExpect(content().string("Async processing of overdue tasks initiated"));
    }
} 