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
    private User anotherUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId("1");
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");

        anotherUser = new User();
        anotherUser.setId("2");
        anotherUser.setUsername("anotherUser");
        anotherUser.setPassword("password456");
        anotherUser.setEmail("another@example.com");
    }

    @Test
    public void getUserByUsername_UserExists_ReturnsUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.getUserByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void getUserByUsername_UserNotExists_ReturnsNull() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);

        User result = userService.getUserByUsername("nonExistentUser");

        assertNull(result);
        verify(userRepository).findByUsername("nonExistentUser");
    }

    @Test
    public void getUserByUsername_EmptyUsername_ReturnsNull() {
        when(userRepository.findByUsername("")).thenReturn(null);

        User result = userService.getUserByUsername("");

        assertNull(result);
        verify(userRepository).findByUsername("");
    }

    @Test
    public void getUserByUsername_NullUsername_ReturnsNull() {
        when(userRepository.findByUsername(null)).thenReturn(null);

        User result = userService.getUserByUsername(null);

        assertNull(result);
        verify(userRepository).findByUsername(null);
    }

    @Test
    public void createUser_ValidUser_ReturnsCreatedUser() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(testUser);
    }

    @Test
    public void createUser_UserWithoutEmail_ReturnsCreatedUser() {
        User userWithoutEmail = new User();
        userWithoutEmail.setUsername("noEmailUser");
        userWithoutEmail.setPassword("password");
        
        when(userRepository.save(any(User.class))).thenReturn(userWithoutEmail);

        User result = userService.createUser(userWithoutEmail);

        assertNotNull(result);
        assertEquals("noEmailUser", result.getUsername());
        verify(userRepository).save(userWithoutEmail);
    }

    @Test
    public void createUser_NullUser_ThrowsException() {
        when(userRepository.save(null)).thenThrow(new IllegalArgumentException("User cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(null);
        });

        verify(userRepository).save(null);
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

    @Test
    public void login_EmptyPassword_ReturnsNull() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.login("testUser", "");

        assertNull(result);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void login_NullPassword_ReturnsNull() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.login("testUser", null);

        assertNull(result);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void login_UserWithNullPassword_ReturnsNull() {
        User userWithNullPassword = new User();
        userWithNullPassword.setUsername("testUser");
        userWithNullPassword.setPassword(null);
        
        when(userRepository.findByUsername("testUser")).thenReturn(userWithNullPassword);

        User result = userService.login("testUser", "password123");

        assertNull(result);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void login_CaseSensitivePassword_ReturnsNull() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result = userService.login("testUser", "PASSWORD123");

        assertNull(result);
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    public void login_MultipleCallsWithSameCredentials_ReturnsUserEachTime() {
        when(userRepository.findByUsername("testUser")).thenReturn(testUser);

        User result1 = userService.login("testUser", "password123");
        User result2 = userService.login("testUser", "password123");

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("testUser", result1.getUsername());
        assertEquals("testUser", result2.getUsername());
        verify(userRepository, times(2)).findByUsername("testUser");
    }
}
