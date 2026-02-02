# ТЕХНІЧНИЙ ЗВІТ: ІНТЕГРАЦІЯ ТЕХНОЛОГІЇ ORM ТА УПРАВЛІННЯ БД
## Проект "Smart Organizer"

### Загальна інформація
**Тема:** Автоматизація відображення об'єктної моделі на реляційну базу даних.


---

### Опис етапу розробки
**Мета:** Реалізація механізмів автоматичного створення та оновлення схеми бази даних, а також налаштування пулу з'єднань для високої продуктивності.
**Виконані завдання:**
1. Розробка класу `DatabaseInitializer` для автоматичної генерації таблиць (ORM approach).
2. Налаштування пулу з'єднань на базі Apache DBCP2.
3. Реалізація механізму міграцій для додавання нових колонок без втрати існуючих даних.
4. Конфігурація драйвера `mysql-connector-j` для роботи з MySQL 8.0+.

---

### Реалізований підхід (ORM Mechanism)
Замість використання важких ORM-фреймворків, у проекті реалізовано легковажний механізм синхронізації:
1. **Автоматична перевірка:** При старті додатка система перевіряє наявність таблиць.
2. **Динамічне створення:** Якщо таблиця відсутня, виконується DDL-запит на її створення.
3. **Еволюція схеми:** Використовуються блоки `ALTER TABLE` для автоматичного додавання полів, які з'явилися в нових версіях сутностей.
4. **Environment-friendly:** Параметри підключення можуть бути передані через змінні оточення (Docker-compatible).

---

### Реалізація (Фрагмент DatabaseInitializer.java)
```java
package org.example.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Забезпечує автоматичне розгортання структури БД.
 */
public class DatabaseInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void initialize() {
        String baseUrl = DatabaseConfig.getProperty("db.url");
        String dbName = DatabaseConfig.getProperty("db.name");

        try (Connection conn = DriverManager.getConnection(baseUrl, user, pass);
             Statement stmt = conn.createStatement()) {
            
            // Створення БД та таблиць
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            setupTables(stmt);
            
            LOGGER.info("База даних успішно ініціалізована.");
        } catch (Exception e) {
            LOGGER.error("Помилка ініціалізації БД: ", e);
        }
    }

    private static void setupTables(Statement stmt) throws Exception {
        // Логіка створення таблиць на основі Entity Book
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS books (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "file_content LONGBLOB" +
                ")");
    }
}
```

---

### Аналіз та висновки
Впроваджений ORM-підхід забезпечує баланс між простотою та надійністю.
- **Масштабованість:** Система легко адаптується до змін у моделі даних.
- **Стабільність:** Пул з'єднань дозволяє системі ефективно працювати під навантаженням.
- **Зручність:** Розробнику не потрібно вручну виконувати SQL-скрипти при розгортанні нової копії проекту.
