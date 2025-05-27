package com.example.lab.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("mysql")
public class MySQLConfigurationTest {

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
    }

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    void mysqlProfile_IsActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        assertTrue(java.util.Arrays.asList(activeProfiles).contains("mysql"));
    }

    @Test
    void dataSource_IsConfiguredCorrectly() {
        assertNotNull(dataSource);
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            
            // Проверяем что это действительно MySQL
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            assertEquals("MySQL", databaseProductName);
            
        } catch (SQLException e) {
            fail("Failed to get database connection: " + e.getMessage());
        }
    }

    @Test
    void mysqlProperties_AreLoaded() {
        // Проверяем что MySQL драйвер загружен
        String driverClassName = environment.getProperty("spring.datasource.driver-class-name");
        assertEquals("com.mysql.cj.jdbc.Driver", driverClassName);
        
        // Проверяем что Flyway включен
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertEquals("true", flywayEnabled);
        
        // Проверяем Hibernate настройки
        String hibernateDdlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        assertEquals("validate", hibernateDdlAuto);
    }

    @Test
    void databaseConnection_CanExecuteQueries() {
        try (Connection connection = dataSource.getConnection()) {
            // Выполняем простой запрос для проверки соединения
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1 as test_value");
            
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("test_value"));
            
        } catch (SQLException e) {
            fail("Failed to execute test query: " + e.getMessage());
        }
    }

    @Test
    void mysqlContainer_IsRunning() {
        assertTrue(mysql.isRunning());
        assertTrue(mysql.isCreated());
        
        // Проверяем параметры контейнера
        assertEquals("testdb", mysql.getDatabaseName());
        assertEquals("test", mysql.getUsername());
        assertEquals("test", mysql.getPassword());
    }

    @Test
    void flywayConfiguration_IsCorrect() {
        String flywayLocations = environment.getProperty("spring.flyway.locations");
        assertEquals("classpath:db/migration", flywayLocations);
        
        String baselineOnMigrate = environment.getProperty("spring.flyway.baseline-on-migrate");
        assertEquals("true", baselineOnMigrate);
    }

    @Test
    void hibernateDialect_IsMySQL() {
        String hibernateDialect = environment.getProperty("spring.jpa.properties.hibernate.dialect");
        assertEquals("org.hibernate.dialect.MySQLDialect", hibernateDialect);
    }

    @Test
    void showSql_IsEnabled() {
        String showSql = environment.getProperty("spring.jpa.show-sql");
        assertEquals("true", showSql);
    }
} 