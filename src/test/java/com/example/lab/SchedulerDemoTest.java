package com.example.lab;

import com.example.lab.model.Task;
import com.example.lab.repository.TaskRepository;
import com.example.lab.service.SchedulerService;
import com.example.lab.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class SchedulerDemoTest {

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void demonstrateSchedulerFunctionality() {
        // 1. Создаем просроченную задачу
        Task overdueTask = new Task();
        overdueTask.setUserId("demo-user");
        overdueTask.setTitle("Просроченная задача");
        overdueTask.setDescription("Эта задача просрочена");
        overdueTask.setTargetDate(LocalDateTime.now().minusDays(1)); // Вчера
        overdueTask.setCompleted(false);
        overdueTask.setDeleted(false);

        // 2. Создаем обычную задачу
        Task normalTask = new Task();
        normalTask.setUserId("demo-user");
        normalTask.setTitle("Обычная задача");
        normalTask.setDescription("Эта задача не просрочена");
        normalTask.setTargetDate(LocalDateTime.now().plusDays(1)); // Завтра
        normalTask.setCompleted(false);
        normalTask.setDeleted(false);

        // 3. Сохраняем задачи
        taskRepository.save(overdueTask);
        taskRepository.save(normalTask);

        // 4. Проверяем что просроченная задача найдена
        List<Task> overdueTasks = taskService.getOverdueTasks(LocalDateTime.now());
        assertEquals(1, overdueTasks.size());
        assertEquals("Просроченная задача", overdueTasks.get(0).getTitle());

        // 5. Запускаем асинхронную обработку
        schedulerService.processOverdueTasksAsync();

        // 6. Проверяем что планировщик работает
        schedulerService.checkOverdueTasks();

        System.out.println("✅ Демонстрация планировщика завершена успешно!");
        System.out.println("📋 Найдено просроченных задач: " + overdueTasks.size());
        System.out.println("📝 Название просроченной задачи: " + overdueTasks.get(0).getTitle());
    }

    @Test
    void demonstrateAsyncProcessing() {
        // Создаем несколько просроченных задач
        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setUserId("user-" + i);
            task.setTitle("Просроченная задача " + i);
            task.setDescription("Описание задачи " + i);
            task.setTargetDate(LocalDateTime.now().minusHours(i));
            task.setCompleted(false);
            task.setDeleted(false);
            taskRepository.save(task);
        }

        // Проверяем что все задачи найдены
        List<Task> overdueTasks = taskService.getOverdueTasks(LocalDateTime.now());
        assertEquals(3, overdueTasks.size());

        // Запускаем асинхронную обработку
        schedulerService.processOverdueTasksAsync();

        System.out.println("✅ Асинхронная обработка " + overdueTasks.size() + " задач завершена!");
    }
} 