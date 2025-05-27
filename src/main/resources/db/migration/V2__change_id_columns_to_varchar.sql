-- Удаляем внешние ключи, чтобы можно было изменить типы данных
ALTER TABLE tasks DROP FOREIGN KEY tasks_ibfk_1;
ALTER TABLE notifications DROP FOREIGN KEY notifications_ibfk_1;

-- Изменяем тип данных id в таблице users
ALTER TABLE users MODIFY COLUMN id VARCHAR(255) NOT NULL PRIMARY KEY;

-- Изменяем тип данных id в таблице tasks
ALTER TABLE tasks MODIFY COLUMN id VARCHAR(255) NOT NULL PRIMARY KEY;
ALTER TABLE tasks MODIFY COLUMN user_id VARCHAR(255);

-- Изменяем тип данных id в таблице notifications
ALTER TABLE notifications MODIFY COLUMN id VARCHAR(255) NOT NULL PRIMARY KEY;
ALTER TABLE notifications MODIFY COLUMN user_id VARCHAR(255);

-- Восстанавливаем внешние ключи
ALTER TABLE tasks ADD CONSTRAINT tasks_user_fk FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE notifications ADD CONSTRAINT notifications_user_fk FOREIGN KEY (user_id) REFERENCES users(id);