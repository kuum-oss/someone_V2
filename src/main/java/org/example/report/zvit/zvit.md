# ТЕХНІЧНИЙ ЗВІТ: АРХІТЕКТУРА ТА ІНІЦІАЛІЗАЦІЯ ПРОЕКТУ
## Проект "Smart Organizer"

### Загальна інформація
**Тема:** Розробка інформаційної системи "Smart Organizer" з використанням Java 21 та Maven.


---

### Опис етапу розробки
**Напрям:** Створення базової інфраструктури, налаштування архітектурних шарів та управління залежностями.
**Мета:** Побудова масштабованої архітектури (Clean Architecture), що включає шари Entity, Repository, Use Cases та Interface Adapters.
**Виконані завдання:**
1. Ініціалізація проекту та налаштування середовища розробки.
2. Проектування структури пакетів згідно з принципами розділення відповідальності.
3. Конфігурація Maven (`pom.xml`) та підключення критичних бібліотек (Tika, FlatLaf, Javalin, MySQL Driver).
4. Реалізація центральної точки входу (Main) з логікою вибору режиму роботи (GUI/Web).

---

### Технічні результати
Проект реалізовано на базі Java 21 з використанням Maven.
- **Core Layer:** Створено базові сутності `Book`, `User`, `Order` у пакеті `org.example.core.entity`.
- **Infrastructure Layer:** Реалізовано механізми доступу до даних (JDBC) та ініціалізації БД.
- **Application Layer:** Розроблено контролери для управління станом бібліотеки та авторизацією.
- **UI:** Реалізовано подвійний інтерфейс (настільний Swing та веб-сервер Javalin).

---

### Фрагмент коду (Main.java)
```java
package org.example;

import org.example.infrastructure.ui.BookLibraryGui;
import org.example.infrastructure.db.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;

/**
 * Точка входу в систему. 
 * Забезпечує ініціалізацію БД та запуск обраного інтерфейсу.
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting application...");
            DatabaseInitializer.initialize();

            if (java.awt.GraphicsEnvironment.isHeadless()) {
                startWebServer();
                return;
            }

            String[] options = {"Приложение (GUI)", "Сайт (Web)"};
            int selection = JOptionPane.showOptionDialog(null, 
                    "Выберите режим запуска:", 
                    "Smart Organizer Setup", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, options, options[0]);

            if (selection == 1) {
                startWebServer();
            } else if (selection == 0) {
                startGui();
            }
        } catch (Exception e) {
            LOGGER.error("Fatal error during startup", e);
        }
    }
}
```

---

### Аналіз та висновки
В результаті розробки створено стабільний фундамент системи. Завдяки Clean Architecture досягнуто повної незалежності бізнес-логіки від зовнішніх інструментів.
- **Ефективність:** Використання Maven забезпечує автоматизацію збірки та управління життєвим циклом проекту.
- **Гнучкість:** Система готова до розширення функціоналу без кардинальних змін в існуючій структурі.
- **Масштабованість:** Чіткий розподіл на пакети полегшує підтримку та тестування.
