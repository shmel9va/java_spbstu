package com.example.lab.repository;

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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryIntegrationTest {

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
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
    }

    @Test
    void findByUsername_UserExists_ReturnsUser() {
        // Сохраняем пользователя через EntityManager
        entityManager.persistAndFlush(testUser);

        // Тестируем поиск
        User found = userRepository.findByUsername("testUser");

        assertNotNull(found);
        assertEquals("testUser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void findByUsername_UserNotExists_ReturnsNull() {
        User found = userRepository.findByUsername("nonExistentUser");

        assertNull(found);
    }

    @Test
    void save_ValidUser_SavesSuccessfully() {
        User saved = userRepository.save(testUser);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("testUser", saved.getUsername());
        assertEquals("test@example.com", saved.getEmail());

        // Проверяем что пользователь действительно сохранен в БД
        User found = entityManager.find(User.class, saved.getId());
        assertNotNull(found);
        assertEquals("testUser", found.getUsername());
    }

    @Test
    void save_UserWithSameUsername_SavesSuccessfully() {
        // Сохраняем первого пользователя
        userRepository.save(testUser);

        // Создаем второго пользователя с тем же username
        User anotherUser = new User();
        anotherUser.setUsername("testUser");
        anotherUser.setPassword("differentPassword");
        anotherUser.setEmail("another@example.com");

        // В зависимости от реализации, может быть ограничение уникальности
        User saved = userRepository.save(anotherUser);
        assertNotNull(saved);
    }

    @Test
    void findByUsername_CaseSensitive_ReturnsCorrectUser() {
        entityManager.persistAndFlush(testUser);

        User found = userRepository.findByUsername("TESTUSER");
        // В зависимости от настроек MySQL, может быть case-sensitive или нет
        // Этот тест проверяет поведение
        assertNull(found); // Предполагаем case-sensitive поиск
    }

    @Test
    void save_UserWithNullEmail_SavesSuccessfully() {
        testUser.setEmail(null);

        User saved = userRepository.save(testUser);

        assertNotNull(saved);
        assertEquals("testUser", saved.getUsername());
        assertNull(saved.getEmail());
    }

    @Test
    void multipleUsers_FindByUsername_ReturnsCorrectUser() {
        // Создаем несколько пользователей
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("pass1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("pass2");
        user2.setEmail("user2@example.com");

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(testUser);

        // Тестируем поиск каждого пользователя
        User foundUser1 = userRepository.findByUsername("user1");
        User foundUser2 = userRepository.findByUsername("user2");
        User foundTestUser = userRepository.findByUsername("testUser");

        assertNotNull(foundUser1);
        assertNotNull(foundUser2);
        assertNotNull(foundTestUser);

        assertEquals("user1@example.com", foundUser1.getEmail());
        assertEquals("user2@example.com", foundUser2.getEmail());
        assertEquals("test@example.com", foundTestUser.getEmail());
    }
} 