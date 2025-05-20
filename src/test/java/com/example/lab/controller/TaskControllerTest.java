package com.example.lab.controller;

import com.example.lab.model.Task;
import com.example.lab.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Task testTask;
    private final String userId = "user123";

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        testTask = new Task();
        testTask.setId("task1");
        testTask.setUserId(userId);
        testTask.setTitle("Тестовая задача");
        testTask.setDescription("Описание тестовой задачи");
        testTask.setCompleted(false);
    }

    @Test
    public void getAllUserTasks_TasksExist_ReturnsTasks() throws Exception {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getAllTasksByUserId(anyString())).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Тестовая задача"))
                .andExpect(jsonPath("$[0].userId").value(userId));
    }

    @Test
    public void getPendingUserTasks_PendingTasksExist_ReturnsPendingTasks() throws Exception {
        // Arrange
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getPendingTasksByUserId(anyString())).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/{userId}/pending", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Тестовая задача"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    public void createTask_ValidTask_ReturnsCreatedTask() throws Exception {
        // Arrange
        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Тестовая задача"));
    }

    @Test
    public void deleteTask_TaskExists_ReturnsNoContent() throws Exception {
        // Arrange
        String taskId = "task1";
        doNothing().when(taskService).deleteTask(taskId);

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
    }
}
