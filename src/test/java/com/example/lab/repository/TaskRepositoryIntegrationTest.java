package com.example.lab.repository;

import com.example.lab.model.Task;
import com.example.lab.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TaskRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User testUser;
    private User anotherUser;
    private Task pendingTask;
    private Task completedTask;
    private Task deletedTask;

    @BeforeEach
    void setUp() {
        // Создаем пользователей
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");

        anotherUser = new User();
        anotherUser.setUsername("anotherUser");
        anotherUser.setPassword("password");
        anotherUser.setEmail("another@example.com");

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(anotherUser);

        // Создаем задачи
        pendingTask = new Task();
        pendingTask.setUserId(testUser.getId());
        pendingTask.setTitle("Pending Task");
        pendingTask.setDescription("Description of pending task");
        pendingTask.setCompleted(false);
        pendingTask.setDeleted(false);

        completedTask = new Task();
        completedTask.setUserId(testUser.getId());
        completedTask.setTitle("Completed Task");
        completedTask.setDescription("Description of completed task");
        completedTask.setCompleted(true);
        completedTask.setDeleted(false);

        deletedTask = new Task();
        deletedTask.setUserId(testUser.getId());
        deletedTask.setTitle("Deleted Task");
        deletedTask.setDescription("Description of deleted task");
        deletedTask.setCompleted(false);
        deletedTask.setDeleted(true);
    }

    @Test
    void findByUserId_TasksExist_ReturnsAllUserTasks() {
        entityManager.persistAndFlush(pendingTask);
        entityManager.persistAndFlush(completedTask);
        entityManager.persistAndFlush(deletedTask);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        
        // Проверяем что все задачи принадлежат правильному пользователю
        tasks.forEach(task -> assertEquals(testUser.getId(), task.getUserId()));
    }

    @Test
    void findByUserId_NoTasks_ReturnsEmptyList() {
        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertNotNull(tasks);
        assertEquals(0, tasks.size());
    }

    @Test
    void findByUserId_DifferentUser_ReturnsOnlyUserTasks() {
        // Создаем задачу для другого пользователя
        Task anotherUserTask = new Task();
        anotherUserTask.setUserId(anotherUser.getId());
        anotherUserTask.setTitle("Another User Task");
        anotherUserTask.setCompleted(false);
        anotherUserTask.setDeleted(false);

        entityManager.persistAndFlush(pendingTask);
        entityManager.persistAndFlush(anotherUserTask);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(testUser.getId(), tasks.get(0).getUserId());
        assertEquals("Pending Task", tasks.get(0).getTitle());
    }

    @Test
    void findPendingByUserId_PendingTasksExist_ReturnsOnlyPendingTasks() {
        entityManager.persistAndFlush(pendingTask);
        entityManager.persistAndFlush(completedTask);
        entityManager.persistAndFlush(deletedTask);

        List<Task> pendingTasks = taskRepository.findPendingByUserId(testUser.getId());

        assertNotNull(pendingTasks);
        assertEquals(1, pendingTasks.size());
        assertEquals("Pending Task", pendingTasks.get(0).getTitle());
        assertEquals(false, pendingTasks.get(0).isCompleted());
        assertEquals(false, pendingTasks.get(0).isDeleted());
    }

    @Test
    void findPendingByUserId_NoPendingTasks_ReturnsEmptyList() {
        entityManager.persistAndFlush(completedTask);
        entityManager.persistAndFlush(deletedTask);

        List<Task> pendingTasks = taskRepository.findPendingByUserId(testUser.getId());

        assertNotNull(pendingTasks);
        assertEquals(0, pendingTasks.size());
    }

    @Test
    void save_ValidTask_SavesSuccessfully() {
        Task saved = taskRepository.save(pendingTask);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Pending Task", saved.getTitle());
        assertEquals(testUser.getId(), saved.getUserId());

        // Проверяем что задача действительно сохранена в БД
        Task found = entityManager.find(Task.class, saved.getId());
        assertNotNull(found);
        assertEquals("Pending Task", found.getTitle());
    }

    @Test
    void findById_TaskExists_ReturnsTask() {
        Task saved = entityManager.persistAndFlush(pendingTask);

        Optional<Task> found = taskRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Pending Task", found.get().getTitle());
        assertEquals(testUser.getId(), found.get().getUserId());
    }

    @Test
    void findById_TaskNotExists_ReturnsEmpty() {
        Optional<Task> found = taskRepository.findById("nonExistentId");

        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_TaskExists_DeletesTask() {
        Task saved = entityManager.persistAndFlush(pendingTask);
        String taskId = saved.getId();

        taskRepository.deleteById(taskId);
        entityManager.flush();

        // Проверяем что задача удалена
        Task found = entityManager.find(Task.class, taskId);
        assertNull(found);
    }

    @Test
    void save_UpdateExistingTask_UpdatesSuccessfully() {
        Task saved = entityManager.persistAndFlush(pendingTask);
        
        // Обновляем задачу
        saved.setTitle("Updated Task Title");
        saved.setCompleted(true);
        
        Task updated = taskRepository.save(saved);

        assertNotNull(updated);
        assertEquals("Updated Task Title", updated.getTitle());
        assertEquals(true, updated.isCompleted());

        // Проверяем что изменения сохранены в БД
        Task found = entityManager.find(Task.class, saved.getId());
        assertEquals("Updated Task Title", found.getTitle());
        assertEquals(true, found.isCompleted());
    }

    @Test
    void bulkOperations_MultipleTasksForMultipleUsers_WorksCorrectly() {
        // Создаем множество задач для разных пользователей
        for (int i = 0; i < 5; i++) {
            Task task1 = new Task();
            task1.setUserId(testUser.getId());
            task1.setTitle("Task " + i + " for testUser");
            task1.setCompleted(i % 2 == 0);
            task1.setDeleted(false);

            Task task2 = new Task();
            task2.setUserId(anotherUser.getId());
            task2.setTitle("Task " + i + " for anotherUser");
            task2.setCompleted(i % 3 == 0);
            task2.setDeleted(false);

            entityManager.persistAndFlush(task1);
            entityManager.persistAndFlush(task2);
        }

        // Проверяем что каждый пользователь видит только свои задачи
        List<Task> testUserTasks = taskRepository.findByUserId(testUser.getId());
        List<Task> anotherUserTasks = taskRepository.findByUserId(anotherUser.getId());

        assertEquals(5, testUserTasks.size());
        assertEquals(5, anotherUserTasks.size());

        // Проверяем pending задачи
        List<Task> testUserPending = taskRepository.findPendingByUserId(testUser.getId());
        List<Task> anotherUserPending = taskRepository.findPendingByUserId(anotherUser.getId());

        // testUser: задачи 1, 3 не завершены (i % 2 != 0)
        assertEquals(2, testUserPending.size());
        // anotherUser: задачи 1, 2, 4 не завершены (i % 3 != 0)
        assertEquals(3, anotherUserPending.size());
    }

    @Test
    void save_TaskWithNullDescription_SavesSuccessfully() {
        pendingTask.setDescription(null);

        Task saved = taskRepository.save(pendingTask);

        assertNotNull(saved);
        assertEquals("Pending Task", saved.getTitle());
        assertNull(saved.getDescription());
    }

    @Test
    void findByUserId_IncludesDeletedTasks_ReturnsAllTasks() {
        entityManager.persistAndFlush(pendingTask);
        entityManager.persistAndFlush(deletedTask);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        // findByUserId должен возвращать все задачи, включая удаленные
        assertEquals(2, tasks.size());
    }
} 