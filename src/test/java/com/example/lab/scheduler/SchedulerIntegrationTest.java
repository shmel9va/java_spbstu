package com.example.lab.scheduler;

import com.example.lab.model.Notification;
import com.example.lab.model.Task;
import com.example.lab.model.User;
import com.example.lab.service.NotificationService;
import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskService;
import com.example.lab.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("mysql")
public class SchedulerIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("scheduler_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setUsername("scheduler_test_user");
        testUser.setPassword("password");
        testUser.setEmail("scheduler@test.com");
        testUser = userService.createUser(testUser);
    }

    @Test
    void testSchedulerFindsOverdueTasks() throws InterruptedException {
        // Arrange - создаем просроченную задачу
        Task overdueTask = new Task(testUser.getId(), 
                                   "Просроченная задача", 
                                   "Для тестирования планировщика",
                                   LocalDateTime.now().minusHours(2));
        Task savedTask = taskService.createTask(overdueTask);

        // Создаем обычную задачу (не просроченную)
        Task normalTask = new Task(testUser.getId(),
                                  "Обычная задача",
                                  "Не должна быть найдена",
                                  LocalDateTime.now().plusDays(1));
        taskService.createTask(normalTask);

        // Act - запускаем планировщик вручную
        schedulerService.checkOverdueTasks();

        // Assert - проверяем что уведомление создано асинхронно
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   List<Notification> notifications = notificationService.getAllNotificationsByUserId(testUser.getId());
                   long overdueNotifications = notifications.stream()
                           .filter(n -> n.getMessage().contains("просрочена"))
                           .count();
                   assertTrue(overdueNotifications >= 1, "Должно быть создано уведомление о просроченной задаче");
               });
    }

    @Test
    void testSchedulerIgnoresCompletedTasks() {
        // Arrange - создаем просроченную но завершенную задачу
        Task completedTask = new Task(testUser.getId(),
                                     "Завершенная просроченная задача",
                                     "Не должна вызывать уведомления",
                                     LocalDateTime.now().minusHours(1));
        completedTask.setCompleted(true);
        taskService.createTask(completedTask);

        int notificationsBefore = notificationService.getAllNotificationsByUserId(testUser.getId()).size();

        // Act
        schedulerService.checkOverdueTasks();

        // Assert - количество уведомлений не должно увеличиться
        await().atMost(5, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   int notificationsAfter = notificationService.getAllNotificationsByUserId(testUser.getId()).size();
                   assertEquals(notificationsBefore, notificationsAfter, 
                               "Завершенные задачи не должны создавать уведомления");
               });
    }

    @Test
    void testAsyncTaskCompletion() {
        // Arrange - создаем несколько задач
        Task task1 = taskService.createTask(new Task(testUser.getId(), "Задача 1", "Описание", LocalDateTime.now().plusDays(1)));
        Task task2 = taskService.createTask(new Task(testUser.getId(), "Задача 2", "Описание", LocalDateTime.now().plusDays(1)));
        Task task3 = taskService.createTask(new Task(testUser.getId(), "Задача 3", "Описание", LocalDateTime.now().plusDays(1)));

        List<String> taskIds = Arrays.asList(task1.getId(), task2.getId(), task3.getId());

        // Act - асинхронно помечаем задачи как завершенные
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> 
            schedulerService.markTasksAsCompletedAsync(taskIds)
        );

        // Assert
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   Task updatedTask1 = taskService.getTaskById(task1.getId());
                   Task updatedTask2 = taskService.getTaskById(task2.getId());
                   Task updatedTask3 = taskService.getTaskById(task3.getId());

                   assertTrue(updatedTask1.isCompleted(), "Задача 1 должна быть завершена");
                   assertTrue(updatedTask2.isCompleted(), "Задача 2 должна быть завершена");
                   assertTrue(updatedTask3.isCompleted(), "Задача 3 должна быть завершена");
               });

        // Проверяем что операция завершилась без ошибок
        assertDoesNotThrow(() -> future.get(10, TimeUnit.SECONDS));
    }

    @Test
    void testSchedulerWithMultipleOverdueTasks() {
        // Arrange - создаем несколько просроченных задач
        for (int i = 1; i <= 5; i++) {
            Task overdueTask = new Task(testUser.getId(),
                                       "Просроченная задача " + i,
                                       "Описание " + i,
                                       LocalDateTime.now().minusHours(i));
            taskService.createTask(overdueTask);
        }

        // Act
        schedulerService.checkOverdueTasks();

        // Assert - должно быть создано 5 уведомлений
        await().atMost(15, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   List<Notification> notifications = notificationService.getAllNotificationsByUserId(testUser.getId());
                   long overdueNotifications = notifications.stream()
                           .filter(n -> n.getMessage().contains("просрочена"))
                           .count();
                   assertEquals(5, overdueNotifications, "Должно быть создано 5 уведомлений о просроченных задачах");
               });
    }

    @Test
    void testAsyncNotificationCreation() {
        // Arrange
        Task overdueTask = new Task(testUser.getId(),
                                   "Асинхронная задача",
                                   "Тест асинхронности",
                                   LocalDateTime.now().minusHours(1));
        Task savedTask = taskService.createTask(overdueTask);

        // Засекаем время
        long startTime = System.currentTimeMillis();

        // Act - создаем уведомление асинхронно
        schedulerService.createOverdueNotificationAsync(savedTask);

        // Assert - метод должен вернуться быстро (асинхронность)
        long executionTime = System.currentTimeMillis() - startTime;
        assertTrue(executionTime < 1000, "Асинхронный метод должен вернуться быстро");

        // Проверяем что уведомление создалось
        await().atMost(10, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   List<Notification> notifications = notificationService.getAllNotificationsByUserId(testUser.getId());
                   assertTrue(notifications.stream()
                                         .anyMatch(n -> n.getTaskId().equals(savedTask.getId())),
                             "Уведомление должно быть создано");
               });
    }
} 