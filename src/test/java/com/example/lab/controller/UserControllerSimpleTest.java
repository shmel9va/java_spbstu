package com.example.lab.controller;

import com.example.lab.model.User;
import com.example.lab.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerSimpleTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

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
    void getUserByUsername_UserExists_ReturnsUser() {
        when(userService.getUserByUsername("testUser")).thenReturn(testUser);

        ResponseEntity<User> response = userController.getUserByUsername("testUser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService).getUserByUsername("testUser");
    }

    @Test
    void getUserByUsername_UserNotExists_ReturnsNotFound() {
        when(userService.getUserByUsername("nonExistentUser")).thenReturn(null);

        ResponseEntity<User> response = userController.getUserByUsername("nonExistentUser");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userService).getUserByUsername("nonExistentUser");
    }

    @Test
    void getUserByUsername_ServiceThrowsException_ReturnsInternalServerError() {
        when(userService.getUserByUsername("testUser"))
                .thenThrow(new RuntimeException("Database connection error"));

        ResponseEntity<User> response = userController.getUserByUsername("testUser");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).getUserByUsername("testUser");
    }

    @Test
    void createUser_ValidUser_ReturnsCreatedUser() {
        when(userService.createUser(any(User.class))).thenReturn(testUser);

        ResponseEntity<User> response = userController.createUser(testUser);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService).createUser(testUser);
    }

    @Test
    void createUser_ServiceThrowsException_ReturnsInternalServerError() {
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        ResponseEntity<User> response = userController.createUser(testUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).createUser(testUser);
    }

    @Test
    void login_ValidCredentials_ReturnsUser() {
        when(userService.login("testUser", "password123")).thenReturn(testUser);

        ResponseEntity<User> response = userController.login("testUser", "password123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService).login("testUser", "password123");
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        when(userService.login("testUser", "wrongPassword")).thenReturn(null);

        ResponseEntity<User> response = userController.login("testUser", "wrongPassword");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(userService).login("testUser", "wrongPassword");
    }

    @Test
    void login_NullUsername_ReturnsBadRequest() {
        ResponseEntity<User> response = userController.login(null, "password123");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).login(any(), any());
    }

    @Test
    void login_NullPassword_ReturnsBadRequest() {
        ResponseEntity<User> response = userController.login("testUser", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).login(any(), any());
    }

    @Test
    void login_ServiceThrowsException_ReturnsInternalServerError() {
        when(userService.login("testUser", "password123"))
                .thenThrow(new RuntimeException("Database connection error"));

        ResponseEntity<User> response = userController.login("testUser", "password123");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).login("testUser", "password123");
    }
} 