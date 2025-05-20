package com.example.lab.service;

import com.example.lab.model.User;
import com.example.lab.repository.UserRepository;
import com.example.lab.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
    }

    @Test
    public void getUserByUsername_UserExists_ReturnsUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.getUserByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void createUser_ValidUser_ReturnsCreatedUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).save(testUser);
    }

    @Test
    public void login_ValidCredentials_ReturnsUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.login("testUser", "password123");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void login_InvalidPassword_ReturnsNull() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.login("testUser", "wrongPassword");

        assertNull(result);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void login_UserNotFound_ReturnsNull() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(null);

        User result = userService.login("nonExistingUser", "password123");

        assertNull(result);
        verify(userRepository).findByUsername("nonExistingUser");
    }
}
