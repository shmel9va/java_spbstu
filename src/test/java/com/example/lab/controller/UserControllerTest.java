package com.example.lab.controller;

import com.example.lab.model.User;
import com.example.lab.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
    }

    @Test
    public void login_ValidCredentials_ReturnsUser() throws Exception {
        // Arrange
        when(userService.login(anyString(), anyString())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/users/login")
                .param("username", "testUser")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    public void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Arrange
        when(userService.login(anyString(), anyString())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/users/login")
                .param("username", "testUser")
                .param("password", "wrongPassword"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void register_ValidUser_ReturnsCreatedUser() throws Exception {
        // Arrange
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testUser"));
    }
}
