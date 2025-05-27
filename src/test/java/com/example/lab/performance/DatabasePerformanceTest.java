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
    void bulkUserCreation_PerformanceTest() {
        long startTime = System.currentTimeMillis();

        List<User> createdUsers = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            User user = new User();
            user.setUsername("bulkUser" + i);
            user.setPassword("password" + i);
            user.setEmail("bulk" + i + "@example.com");

            User savedUser = userService.createUser(user);
            createdUsers.add(savedUser);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertEquals(50, createdUsers.size());
        assertTrue(duration < 30000, "Creating users took " + duration + "ms");

        System.out.println("Created " + createdUsers.size() + " users in " + duration + "ms");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void bulkTaskCreation_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        List<Task> createdTasks = new ArrayList<>();
        
        // Используем первых 10 пользователей для создания задач
        for (User user : testUsers.subList(0, 10)) {
            for (int i = 0; i < TASKS_PER_USER; i++) {
                Task task = new Task();
                task.setUserId(user.getId());
                task.setTitle("Task " + i + " for " + user.getUsername());
                task.setDescription("Description for task " + i);
                task.setCompleted(i % 5 == 0);
                task.setDeleted(false);
                
                Task savedTask = taskService.createTask(task);
                createdTasks.add(savedTask);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertEquals(10 * TASKS_PER_USER, createdTasks.size());
        assertTrue(duration < 60000, "Creating tasks took " + duration + "ms");

        System.out.println("Created " + createdTasks.size() + " tasks in " + duration + "ms");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void bulkTaskRetrieval_PerformanceTest() {
        // Сначала создаем задачи для тестирования
        User testUser = testUsers.get(0);
        for (int i = 0; i < 100; i++) {
            Task task = new Task();
            task.setUserId(testUser.getId());
            task.setTitle("Retrieval Test Task " + i);
            task.setCompleted(i % 3 == 0);
            task.setDeleted(false);

            taskService.createTask(task);
        }
        
        long startTime = System.currentTimeMillis();
        
        // Тестируем получение задач
        for (int i = 0; i < 50; i++) {
            List<Task> allTasks = taskService.getAllTasksByUserId(testUser.getId());
            List<Task> pendingTasks = taskService.getPendingTasksByUserId(testUser.getId());

            assertNotNull(allTasks);
            assertNotNull(pendingTasks);
            assertTrue(allTasks.size() >= 100);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration < 10000, "Retrieving tasks took " + duration + "ms");
        
        System.out.println("Retrieved tasks 50 times in " + duration + "ms");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void complexWorkflow_PerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        // Комплексный тест: создание пользователя, задач и их получение
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
            
            // Получаем уведомления (созданные автоматически при создании задач через Kafka)
            // Примечание: в тестах Kafka может быть недоступен, поэтому проверяем что метод работает
            List<Notification> notifications = notificationService.getAllNotificationsByUserId(savedUser.getId());
            assertNotNull(notifications);

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