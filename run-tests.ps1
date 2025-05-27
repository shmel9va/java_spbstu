# Скрипт для запуска тестов с Docker инфраструктурой

Write-Host "Запуск тестов с Docker инфраструктурой..." -ForegroundColor Green

# Установка переменных окружения
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/taskmanager?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "password"
$env:SPRING_DATA_REDIS_HOST = "localhost"
$env:SPRING_DATA_REDIS_PORT = "6379"
$env:SPRING_KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"

Write-Host "Переменные окружения установлены:" -ForegroundColor Yellow
Write-Host "SPRING_PROFILES_ACTIVE: $env:SPRING_PROFILES_ACTIVE"
Write-Host "SPRING_DATASOURCE_URL: $env:SPRING_DATASOURCE_URL"
Write-Host "SPRING_KAFKA_BOOTSTRAP_SERVERS: $env:SPRING_KAFKA_BOOTSTRAP_SERVERS"

Write-Host ""
Write-Host "Запуск тестов..." -ForegroundColor Green
./gradlew test --info

Write-Host ""
Write-Host "Тесты завершены!" -ForegroundColor Green 