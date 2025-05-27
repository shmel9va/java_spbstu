package com.example.lab.controller;

import com.example.lab.config.TestCacheConfig;
import com.example.lab.model.User;
import com.example.lab.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestCacheConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
    }

    @Test
    void getUserByUsername_UserExists_ReturnsUser() throws Exception {
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);

        mockMvc.perform(get("/api/users/testUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").value("password123"));

        verify(userService).getUserByUsername("testUser");
    }

    @Test
    void getUserByUsername_UserNotExists_ReturnsNotFound() throws Exception {
        when(userService.getUserByUsername("nonExistentUser")).thenReturn(null);

        mockMvc.perform(get("/api/users/nonExistentUser"))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUsername("nonExistentUser");
    }

    @Test
    void getUserByUsername_EmptyUsername_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/"))
                .andExpect(status().isNotFound()); // Spring возвращает 404 для пустого пути

        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    void getUserByUsername_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        when(userService.getUserByUsername("testUser"))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(get("/api/users/testUser"))
                .andExpect(status().isInternalServerError());

        verify(userService).getUserByUsername("testUser");
    }

    @Test
    void createUser_ValidUser_ReturnsCreatedUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void createUser_UserWithoutEmail_ReturnsCreatedUser() throws Exception {
        User userWithoutEmail = new User();
        userWithoutEmail.setId("2");
        userWithoutEmail.setUsername("noEmailUser");
        userWithoutEmail.setPassword("password");

        when(userService.createUser(any(User.class))).thenReturn(userWithoutEmail);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithoutEmail)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("2"))
                .andExpect(jsonPath("$.username").value("noEmailUser"))
                .andExpect(jsonPath("$.email").doesNotExist());

        verify(userService).createUser(any(User.class));
    }

    @Test
    void createUser_InvalidJson_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void createUser_EmptyBody_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    void createUser_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isInternalServerError());

        verify(userService).createUser(any(User.class));
    }

    @Test
    void login_ValidCredentials_ReturnsUser() throws Exception {
        when(userService.login("testUser", "password123")).thenReturn(testUser);

        mockMvc.perform(post("/api/users/login")
                        .param("username", "testUser")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).login("testUser", "password123");
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        when(userService.login("testUser", "wrongPassword")).thenReturn(null);

        mockMvc.perform(post("/api/users/login")
                        .param("username", "testUser")
                        .param("password", "wrongPassword"))
                .andExpect(status().isUnauthorized());

        verify(userService).login("testUser", "wrongPassword");
    }

    @Test
    void login_UserNotFound_ReturnsUnauthorized() throws Exception {
        when(userService.login("nonExistentUser", "password123")).thenReturn(null);

        mockMvc.perform(post("/api/users/login")
                        .param("username", "nonExistentUser")
                        .param("password", "password123"))
                .andExpect(status().isUnauthorized());

        verify(userService).login("nonExistentUser", "password123");
    }

    @Test
    void login_MissingUsername_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("password", "password123"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(), any());
    }

    @Test
    void login_MissingPassword_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .param("username", "testUser"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(), any());
    }

    @Test
    void login_EmptyCredentials_ReturnsUnauthorized() throws Exception {
        when(userService.login("", "")).thenReturn(null);

        mockMvc.perform(post("/api/users/login")
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isUnauthorized());

        verify(userService).login("", "");
    }

    @Test
    void login_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
        when(userService.login("testUser", "password123"))
                .thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/api/users/login")
                        .param("username", "testUser")
                        .param("password", "password123"))
                .andExpect(status().isInternalServerError());

        verify(userService).login("testUser", "password123");
    }

    @Test
    void createUser_UserWithSpecialCharacters_ReturnsCreatedUser() throws Exception {
        User specialUser = new User();
        specialUser.setId("3");
        specialUser.setUsername("user@domain.com");
        specialUser.setPassword("p@ssw0rd!");
        specialUser.setEmail("special+user@example.com");

        when(userService.createUser(any(User.class))).thenReturn(specialUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("user@domain.com"))
                .andExpect(jsonPath("$.email").value("special+user@example.com"));

        verify(userService).createUser(any(User.class));
    }

    @Test
    void getUserByUsername_UsernameWithSpecialCharacters_ReturnsUser() throws Exception {
        testUser.setUsername("user@domain.com");
        when(userService.getUserByUsername("user@domain.com")).thenReturn(testUser);

        mockMvc.perform(get("/api/users/user@domain.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user@domain.com"));

        verify(userService).getUserByUsername("user@domain.com");
    }

    @Test
    void multipleRequests_VerifyServiceInteractions() throws Exception {
        // Создание пользователя
        when(userService.createUser(any(User.class))).thenReturn(testUser);
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)));

        // Получение пользователя
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);
        mockMvc.perform(get("/api/users/testUser"));

        // Логин
        when(userService.login("testUser", "password123")).thenReturn(testUser);
        mockMvc.perform(post("/api/users/login")
                .param("username", "testUser")
                .param("password", "password123"));

        // Проверяем все взаимодействия
        verify(userService).createUser(any(User.class));
        verify(userService).getUserByUsername("testUser");
        verify(userService).login("testUser", "password123");
    }
}
