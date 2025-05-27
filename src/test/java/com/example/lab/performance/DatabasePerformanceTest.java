package com.example.lab.performance;

import com.example.lab.model.Task;
import com.example.lab.model.User;
import com.example.lab.model.Notification;
import com.example.lab.service.TaskService;
import com.example.lab.service.UserService;
import com.example.lab.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("mysql")
public class DatabasePerformanceTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("perftest")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private NotificationService notificationService;

    private List<User> testUsers;
    private static final int USERS_COUNT = 100;
    private static final int TASKS_PER_USER = 50;

    @BeforeEach
    void setUp() {
        testUsers = new ArrayList<>();
        
        // Создаем тестовых пользователей
        for (int i = 0; i < USERS_COUNT; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setPassword("password" + i);
            user.setEmail("user" + i + "@example.com");
            
            User savedUser = userService.createUser(user);
            testUsers.add(savedUser);
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void createManyTasks_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        List<Task> createdTasks = new ArrayList<>();
        
        // Создаем много задач для каждого пользователя
        for (User user : testUsers) {
            for (int i = 0; i < TASKS_PER_USER; i++) {
                Task task = new Task();
                task.setUserId(user.getId());
                task.setTitle("Task " + i + " for " + user.getUsername());
                task.setDescription("Description for task " + i);
                task.setCompleted(i % 3 == 0); // Каждая третья задача завершена
                task.setDeleted(false);
                
                Task savedTask = taskService.createTask(task);
                createdTasks.add(savedTask);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Проверяем что все задачи созданы
        assertEquals(USERS_COUNT * TASKS_PER_USER, createdTasks.size());
        
        // Проверяем производительность (должно быть меньше 30 секунд)
        assertTrue(duration < 30000, "Creating " + (USERS_COUNT * TASKS_PER_USER) + " tasks took " + duration + "ms");
        
        System.out.println("Created " + createdTasks.size() + " tasks in " + duration + "ms");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void retrieveTasksByUser_PerformanceTest() {
        // Сначала создаем задачи
        for (User user : testUsers.subList(0, 10)) { // Берем только 10 пользователей для этого теста
            for (int i = 0; i < TASKS_PER_USER; i++) {
                Task task = new Task();
                task.setUserId(user.getId());
                task.setTitle("Task " + i);
                task.setCompleted(i % 2 == 0);
                task.setDeleted(false);
                
                taskService.createTask(task);
            }
        }
        
        long startTime = System.currentTimeMillis();
        
        // Получаем задачи для каждого пользователя
        for (User user : testUsers.subList(0, 10)) {
            List<Task> userTasks = taskService.getAllTasksByUserId(user.getId());
            assertEquals(TASKS_PER_USER, userTasks.size());
            
            List<Task> pendingTasks = taskService.getPendingTasksByUserId(user.getId());
            assertEquals(TASKS_PER_USER / 2, pendingTasks.size()); // Половина задач не завершена
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 10000, "Retrieving tasks took " + duration + "ms");
        
        System.out.println("Retrieved tasks for 10 users in " + duration + "ms");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void bulkNotificationCreation_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        List<Notification> createdNotifications = new ArrayList<>();
        
        // Создаем уведомления для каждого пользователя
        for (User user : testUsers.subList(0, 20)) { // 20 пользователей
            for (int i = 0; i < 25; i++) { // 25 уведомлений на пользователя
                Notification notification = new Notification(
                    user.getId(),
                    "task" + i,
                    "Notification " + i + " for " + user.getUsername()
                );
                
                Notification savedNotification = notificationService.createNotification(notification);
                createdNotifications.add(savedNotification);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(20 * 25, createdNotifications.size());
        assertTrue(duration < 15000, "Creating notifications took " + duration + "ms");
        
        System.out.println("Created " + createdNotifications.size() + " notifications in " + duration + "ms");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void userLogin_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        // Тестируем логин для всех пользователей
        for (User user : testUsers) {
            User loggedInUser = userService.login(user.getUsername(), "password" + user.getUsername().substring(4));
            assertNotNull(loggedInUser);
            assertEquals(user.getUsername(), loggedInUser.getUsername());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 5000, "Login for " + USERS_COUNT + " users took " + duration + "ms");
        
        System.out.println("Logged in " + USERS_COUNT + " users in " + duration + "ms");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void complexWorkflow_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        // Комплексный тест: создание пользователя, задач, уведомлений и их получение
        for (int i = 0; i < 10; i++) {
            // Создаем пользователя
            User user = new User();
            user.setUsername("perfUser" + i);
            user.setPassword("perfPassword" + i);
            user.setEmail("perf" + i + "@example.com");
            User savedUser = userService.createUser(user);
            
            // Создаем задачи
            List<Task> userTasks = new ArrayList<>();
            for (int j = 0; j < 20; j++) {
                Task task = new Task();
                task.setUserId(savedUser.getId());
                task.setTitle("Performance Task " + j);
                task.setCompleted(j % 4 == 0);
                task.setDeleted(false);
                
                Task savedTask = taskService.createTask(task);
                userTasks.add(savedTask);
            }
            
            // Получаем задачи
            List<Task> retrievedTasks = taskService.getAllTasksByUserId(savedUser.getId());
            assertEquals(20, retrievedTasks.size());
            
            List<Task> pendingTasks = taskService.getPendingTasksByUserId(savedUser.getId());
            assertEquals(15, pendingTasks.size()); // 15 незавершенных задач
            
            // Получаем уведомления (созданные автоматически при создании задач)
            List<Notification> notifications = notificationService.getAllNotificationsByUserId(savedUser.getId());
            assertEquals(20, notifications.size()); // По одному уведомлению на задачу
            
            // Логин
            User loggedInUser = userService.login(savedUser.getUsername(), "perfPassword" + i);
            assertNotNull(loggedInUser);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 20000, "Complex workflow took " + duration + "ms");
        
        System.out.println("Complex workflow for 10 users completed in " + duration + "ms");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void concurrentUserCreation_PerformanceTest() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        
        List<Thread> threads = new ArrayList<>();
        List<User> concurrentUsers = new ArrayList<>();
        
        // Создаем 10 потоков, каждый создает 10 пользователей
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    User user = new User();
                    user.setUsername("concurrent" + threadId + "_" + j);
                    user.setPassword("password");
                    user.setEmail("concurrent" + threadId + "_" + j + "@example.com");
                    
                    User savedUser = userService.createUser(user);
                    synchronized (concurrentUsers) {
                        concurrentUsers.add(savedUser);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        // Ждем завершения всех потоков
        for (Thread thread : threads) {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(100, concurrentUsers.size());
        assertTrue(duration < 10000, "Concurrent user creation took " + duration + "ms");
        
        System.out.println("Created 100 users concurrently in " + duration + "ms");
    }
} 