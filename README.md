# Система управления задачами - Step 1

## Описание
Это простое REST API для управления задачами и уведомлениями пользователей.
Реализованы основные функции создания задач, управления пользователями и уведомлениями.
Данные хранятся в памяти (без использования базы данных).

## Технологии
- Java 17
- Spring Boot
- Gradle

## API Endpoints

### Задачи
- GET /api/tasks/{userId} - получить все задачи пользователя
- GET /api/tasks/{userId}/pending - получить только активные (незавершенные) задачи
- POST /api/tasks - создать новую задачу
- DELETE /api/tasks/{id} - пометить задачу как удаленную

### Пользователи
- GET /api/users/login - авторизация пользователя
- POST /api/users/register - регистрация нового пользователя

### Уведомления
- GET /api/notifications/{userId} - получить все уведомления пользователя
- GET /api/notifications/{userId}/pending - получить непрочитанные уведомления
- POST /api/notifications/{id}/read - пометить уведомление как прочитанное

# Написание unit-тестов - Step 2

## Описание

В рамках шага 2 были реализованы юнит-тесты для всех основных компонентов приложения:


## 1. Подготовка проекта

- Добавлены зависимости в `build.gradle`:
  - **JUnit Jupiter** — для написания тестов
  - **Mockito** — для создания моков
  - **Spring Boot Test** — для интеграции с Spring

## 2. Тесты сервисного слоя

### `UserServiceTest`
- Получение пользователя по имени
- Создание нового пользователя
- Аутентификация (успешная и неуспешная)

### `TaskServiceTest`
- Получение всех задач пользователя
- Получение невыполненных задач
- Создание задачи
- Удаление задачи

### `NotificationServiceTest`
- Получение всех уведомлений
- Получение непрочитанных уведомлений
- Пометка как прочитанное
- Создание уведомления


## 3. Тесты контроллеров

### `UserControllerTest`
- Успешный и неуспешный логин
- Регистрация пользователя

### `TaskControllerTest`
- Получение всех задач
- Получение невыполненных задач
- Создание задачи
- Удаление задачи

### `NotificationControllerTest`
- Получение всех уведомлений
- Получение непрочитанных уведомлений
- Отметка уведомления как прочитанного


## Результат

Все тесты успешно проходят.  
Покрытие охватывает ключевую бизнес-логику сервисов и REST API интерфейсы.
---
# Интеграция с In-Memory базой данных (H2) - Step 3

## Описание

В рамках шага 3 проект был модифицирован для работы с базами данных. Для этого была добавлена поддержка H2 - легковесной in-memory базы данных, и реализована инфраструктура Spring Data JPA.

## Основные изменения:

1. **Добавлена поддержка Spring Data JPA:**
  - Добавлены зависимости для работы с H2 и JPA в `build.gradle`
  - Сущности (`Task`, `User`, `Notification`) теперь аннотированы с `@Entity`
  - Созданы JPA-репозитории для всех сущностей

2. **Реализованы профили для переключения между хранилищами:**
  - `dev` - использует хранилище в памяти (Map) для быстрой разработки
  - `database` - использует H2 базу данных через JPA репозитории

3. **Адаптеры для поддержания совместимости:**
  - `JpaTaskRepositoryAdapter`, `JpaUserRepositoryAdapter`, `JpaNotificationRepositoryAdapter` - связывают интерфейсы базовых репозиториев с JPA имплементациями

---
# Добавление поддержки Docker - Step 4

## Описание

В рамках шага 4 была добавлена поддержка Docker для запуска приложения в контейнере с использованием H2 базы данных.

## Основные изменения:

1. **Создан оптимизированный Dockerfile:**

2. **Настроен Docker Compose:**

3. **Добавлены настройки H2 для работы в Docker:**

## Как запустить приложение в Docker

1. Соберите и запустите Docker контейнер:
   ```bash
   docker-compose up -d --build
   ```

2. Проверьте статус контейнера:
   ```bash
   docker-compose ps
   ```

3. Просмотр логов приложения:
   ```bash
   docker-compose logs -f app
   ```

4. Остановка контейнера:
   ```bash
   docker-compose down
   ```

---
# Переход на MySQL - Step 5

## Описание

В рамках шага 5 проект был модифицирован для работы с MySQL вместо H2, при этом сохранена возможность использования обеих баз данных через профили Spring.

## Основные изменения:

1. **Добавлена поддержка MySQL:**
   - Добавлены зависимости для работы с MySQL
   - Создан профиль `mysql` для конфигурации MySQL
   - Репозитории настроены для работы с обеими базами данных

2. **Внедрена система миграций Flyway:**
   - Добавлены зависимости Flyway
   - Созданы миграционные скрипты для инициализации схемы базы данных
   - Настроена автоматическая миграция при запуске приложения

3. **Обновлен Docker Compose:**
   - Добавлен сервис MySQL
   - Настроено взаимодействие между сервисами
   - Сохранены данные в именованных томах

## Как переключаться между профилями

Приложение поддерживает следующие профили:
- `dev` - использует хранилище в памяти (Map) для быстрой разработки
- `database` - использует H2 базу данных через JPA репозитории
- `mysql` - использует MySQL базу данных через JPA репозитории с миграциями Flyway

Для запуска с нужным профилем используйте параметр:

---
# Внедрение Redis кэширования - Step 6

## Описание

В рамках шага 6 была реализована система кэширования с использованием Redis для оптимизации производительности операций получения задач. Кэш автоматически обновляется при создании/удалении задач и имеет настраиваемое время жизни.

## Основные изменения:

### 1. **Добавлены зависимости Redis:**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.boot:spring-boot-starter-cache'
```

### 2. **Конфигурация кэширования в application.properties:**
```properties
# Redis Configuration
spring.cache.type=redis
spring.data.redis.host=redis
spring.data.redis.port=6379
spring.data.redis.timeout=2000
spring.data.redis.connect-timeout=2000

# Cache Configuration  
spring.cache.redis.time-to-live=60000        # TTL 60 секунд
spring.cache.cache-names=tasks
spring.cache.redis.key-prefix=task_cache_
spring.cache.redis.use-key-prefix=true
spring.cache.redis.enable-statistics=true
```

### 3. **Включение кэширования:**
- Добавлена аннотация `@EnableCaching` в главный класс `LabApplication`

### 4. **Кэширование в сервисном слое:**
Добавлены аннотации кэширования в `TaskServiceImpl`:

```java
@Cacheable(value = "tasks", key = "'user_' + #userId")
public List<Task> getAllTasksByUserId(String userId)

@Cacheable(value = "tasks", key = "'user_pending_' + #userId")  
public List<Task> getPendingTasksByUserId(String userId)

@Cacheable(value = "tasks", key = "'task_' + #id")
public Task getTaskById(String id)

@CacheEvict(value = "tasks", key = "'user_' + #task.userId")
public Task createTask(Task task)  // Очистка кэша при создании

@CacheEvict(value = "tasks", key = "'task_' + #id")
public Task deleteTask(String id)  // Очистка кэша при удалении
```

### 5. **Обеспечение сериализации:**
- Модель `Task` реализует интерфейс `Serializable` для хранения в Redis

### 6. **Обновлен Docker Compose:**
```yaml
redis:
  image: redis:latest
  ports:
    - "6379:6379"
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

## Как работает кэширование:

1. **Первый запрос** - попадает в базу данных, результат сохраняется в Redis
2. **Последующие запросы** - возвращаются из кэша (быстрее)
3. **Автоматическая очистка** - кэш очищается при создании/удалении задач
4. **TTL (Time To Live)** - записи автоматически удаляются через 60 секунд

## Тестирование кэша:

Для проверки работы кэширования можно:

1. **Запустить приложение:**
   ```bash
   docker-compose up -d --build
   ```

2. **Создать пользователя и задачу:**
   ```bash
   # Создать пользователя
   curl -X POST "http://localhost:8080/api/users" \
     -H "Content-Type: application/json" \
     -d '{"email": "test@example.com", "password": "password123"}'
   
   # Создать задачу  
   curl -X POST "http://localhost:8080/api/tasks" \
     -H "Content-Type: application/json" \
     -d '{"userId": "USER_ID", "title": "Test Task", "description": "Cache test"}'
   ```

3. **Тестировать кэширование:**
   ```bash
   # Первый запрос (попадание в БД)
   curl "http://localhost:8080/api/tasks/user/USER_ID"
   
   # Второй запрос (из кэша - быстрее)
   curl "http://localhost:8080/api/tasks/user/USER_ID"
   ```

4. **Проверить логи приложения:**
   ```bash
   docker logs java_spbstu-app-1 --tail=20
   ```

## Преимущества реализованного кэширования:

- **Повышение производительности** - снижение нагрузки на базу данных
- **Автоматическое управление** - кэш обновляется при изменении данных
- **Настраиваемое время жизни** - гибкая конфигурация TTL
- **Fallback механизм** - при недоступности Redis запросы идут в БД
- **Масштабируемость** - Redis может быть вынесен на отдельный сервер

## Ключи кэша:

- `user_{userId}` - все задачи пользователя
- `user_pending_{userId}` - незавершенные задачи пользователя  
- `task_{taskId}` - конкретная задача по ID

---

## Внедрение Kafka - Step 7

### Описание
В рамках Step 7 была реализована асинхронная система обмена сообщениями с использованием Apache Kafka для полного разделения сервисов и обеспечения надежной доставки уведомлений.

### Основные изменения:

#### 1. **Настройка Kafka инфраструктуры**
- Добавлен Kafka и Zookeeper в `docker-compose.yml`
- Настроена сетевая конфигурация между контейнерами
- Добавлены зависимости Spring Kafka в `build.gradle`

#### 2. **Kafka Events система**
```java
// TaskEvent.java - событие для Kafka
public record TaskEvent(
    TaskEventTypeEnum eventType,
    String taskId,
    String userId
) {}

// TaskEventTypeEnum.java - типы событий
public enum TaskEventTypeEnum {
    CREATE, DELETE
}
```

#### 3. **Producer в TaskService**
```java
@Service
public class TaskServiceImpl implements TaskService {
    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    
    public Task createTask(Task task) {
        Task savedTask = taskRepository.save(task);
        // Отправка события в Kafka
        kafkaTemplate.send(taskEventTopic, 
            new TaskEvent(TaskEventTypeEnum.CREATE, savedTask.getId(), savedTask.getUserId()));
        return savedTask;
    }
}
```

#### 4. **Consumer в NotificationService**
```java
@Service
public class NotificationServiceImpl implements NotificationService {
    
    @KafkaListener(topics = "${kafka.topic.task-event}")
    @Transactional
    public void handleTaskEvent(TaskEvent taskEvent) {
        String message = switch (taskEvent.eventType()) {
            case CREATE -> "Task created!";
            case DELETE -> "Task deleted!";
        };
        Notification notification = new Notification(
            taskEvent.userId(), taskEvent.taskId(), message);
        createNotification(notification);
    }
}
```

#### 5. **Конфигурация Kafka**
```properties
# application.properties
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.group-id=notification-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
kafka.topic.task-event=task-events
```

### Ключевые особенности реализации:

**Полная изоляция сервисов** - NotificationService получает уведомления ТОЛЬКО через Kafka  
**Асинхронная обработка** - события обрабатываются в отдельных потоках  
**Транзакционность** - Kafka listener помечен `@Transactional` для надежности  
**Обработка CREATE/DELETE** - поддержка всех типов событий задач  
**Автоматическое создание топиков** - через KafkaTopicConfig  

### Тестирование Kafka messaging:

1. **Подготовка (создание пользователя):**
```bash
# Создать пользователя
curl -X POST -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@example.com","password":"password123"}' \
     http://localhost:8080/api/users

# Получить пользователя и его ID
curl http://localhost:8080/api/users/testuser
# Скопировать "id" из ответа для следующих команд
```

2. **Тест с работающим Kafka:**
```bash
# Создать задачу (замените USER_ID на реальный ID из предыдущего шага)
curl -X POST -H "Content-Type: application/json" \
     -d '{"title":"Test Task","description":"Test description","userId":"USER_ID"}' \
     http://localhost:8080/api/tasks

# Проверить уведомления (замените USER_ID)
curl http://localhost:8080/api/notifications/USER_ID
# Результат: уведомление создано автоматически через Kafka
```

3. **Тест без Kafka (проверка изоляции):**
```bash
# Остановить Kafka
docker stop java_spbstu-kafka-1

# Создать задачу (замените USER_ID)
curl -X POST -H "Content-Type: application/json" \
     -d '{"title":"Task without Kafka","description":"No notification","userId":"USER_ID"}' \
     http://localhost:8080/api/tasks
# Задача создается успешно

# Проверить уведомления (замените USER_ID)
curl http://localhost:8080/api/notifications/USER_ID
# Результат: новых уведомлений НЕТ - только старые

# Запустить Kafka обратно
docker start java_spbstu-kafka-1
```

---

## Планировщик задач и асинхронная обработка - Step 8

### Описание
В рамках Step 8 была реализована система планировщика задач с использованием `@Scheduled` для периодической проверки просроченных задач и `@Async` для асинхронной обработки уведомлений.

### Основные изменения:

#### 1. **Включение планировщика и асинхронности**
```java
@SpringBootApplication
@EnableCaching
@EnableAsync         // Включение асинхронной обработки
@EnableScheduling    // Включение планировщика задач
public class LabApplication {
    public static void main(String[] args) {
        SpringApplication.run(LabApplication.class, args);
    }
}
```

#### 2. **Создание SchedulerService**
```java
public interface SchedulerService {
    // Периодическая проверка просроченных задач
    void checkOverdueTasks();
    
    // Асинхронное создание уведомления о просроченной задаче
    void createOverdueNotificationAsync(Task task);
    
    // Асинхронная пометка задач как завершенных
    void markTasksAsCompletedAsync(List<String> taskIds);
}
```

#### 3. **Реализация планировщика**
```java
@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Override
    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void checkOverdueTasks() {
        LocalDateTime currentTime = LocalDateTime.now();
        List<Task> overdueTasks = taskService.findOverdueTasks(currentTime);
        
        if (!overdueTasks.isEmpty()) {
            for (Task task : overdueTasks) {
                createOverdueNotificationAsync(task);
            }
        }
    }

    @Override
    @Async
    public void createOverdueNotificationAsync(Task task) {
        String message = String.format("задача '%s' просрочена! целевая дата: %s", 
                                      task.getTitle(), task.getTargetDate());
        
        Notification notification = new Notification();
        notification.setUserId(task.getUserId());
        notification.setTaskId(task.getId());
        notification.setMessage(message);
        
        notificationService.createNotification(notification);
    }
}
```

#### 4. **Расширение TaskService**
Добавлены методы для работы с просроченными задачами:
```java
public interface TaskService {
    // Существующие методы...
    
    // Новые методы для планировщика
    List<Task> findOverdueTasks(LocalDateTime currentTime);
    Task markTaskAsCompleted(String taskId);
}
```

#### 5. **Обновление репозиториев**
Добавлен метод поиска просроченных задач:
```java
// JPA Repository
@Query("SELECT t FROM Task t WHERE t.targetDate < :currentTime AND t.completed = false AND t.deleted = false")
List<Task> findOverdueTasks(@Param("currentTime") LocalDateTime currentTime);

// InMemory Repository  
public List<Task> findOverdueTasks(LocalDateTime currentTime) {
    return tasks.values().stream()
            .filter(task -> task.getTargetDate() != null && 
                           task.getTargetDate().isBefore(currentTime) &&
                           !task.isCompleted() && 
                           !task.isDeleted())
            .collect(Collectors.toList());
}
```

#### 6. **Новый API эндпоинт**
Добавлен эндпоинт для пометки задач как завершенных:
```java
@PutMapping("/{id}/complete")
public ResponseEntity<Task> markTaskAsCompleted(@PathVariable String id) {
    Task completedTask = taskService.markTaskAsCompleted(id);
    if (completedTask != null) {
        return ResponseEntity.ok(completedTask);
    }
    return ResponseEntity.notFound().build();
}
```

### Как работает планировщик:

1. **Автоматический запуск** - каждую минуту (60000 мс) планировщик проверяет просроченные задачи
2. **Поиск просроченных задач** - находит все задачи, где `targetDate < currentTime` и задача не завершена
3. **Асинхронное создание уведомлений** - для каждой просроченной задачи создается уведомление в отдельном потоке
4. **Логирование** - все операции планировщика логируются в консоль

### Тестирование планировщика:

1. **Создать задачу с просроченной датой:**
```bash
curl -X POST -H "Content-Type: application/json" \
     -d '{
       "title":"Просроченная задача",
       "description":"Тест планировщика",
       "userId":"USER_ID",
       "targetDate":"2024-01-01T10:00:00"
     }' \
     http://localhost:8080/api/tasks
```

2. **Запустить приложение и наблюдать логи:**
```bash
docker-compose up -d --build
docker logs -f java_spbstu-app-1
```

3. **Проверить созданные уведомления:**
```bash
curl http://localhost:8080/api/notifications/USER_ID
```

4. **Пометить задачу как завершенную:**
```bash
curl -X PUT http://localhost:8080/api/tasks/TASK_ID/complete
```

### Написанные тесты:

- **SchedulerServiceTest** - тесты для планировщика
- **Расширенный TaskServiceTest** - тесты для новых методов поиска просроченных задач
- **Полное покрытие** - все новые методы покрыты unit-тестами

### Ключевые особенности:

- **Фоновая работа** - планировщик работает автоматически без вмешательства пользователя
- **Асинхронность** - уведомления создаются в отдельных потоках для повышения производительности
- **Масштабируемость** - можно легко изменить интервал проверки или добавить дополнительную логику
- **Отказоустойчивость** - обработка ошибок при создании уведомлений
- **Кэширование** - новые методы интегрированы с системой кэширования Redis

