# ТЕХНІЧНИЙ ЗВІТ: РОЗРОБКА КЛАСІВ СУТНОСТЕЙ
## Проект "Smart Organizer"

### Загальна інформація
**Тема:** Проектування та реалізація об'єктної моделі предметної області.


---

### Опис етапу розробки
**Мета:** Створення чистих Java-об'єктів (POJO), які представляють основні сутності системи та інкапсулюють бізнес-правила.
**Виконані завдання:**
1. Визначення ключових атрибутів для сутностей `Book`, `User`, `Order`.
2. Впровадження паттерну **Builder** для гнучкого створення об'єктів.
3. Забезпечення відповідності між об'єктами в пам'яті та реляційними таблицями.
4. Реалізація методів `equals`, `hashCode` та `toString` для коректної роботи в колекціях.

---

### Структура сутностей

1. **User:**
   - Ідентифікатор (`id`).
   - Контактні дані (`email`).
   - Безпека (хешований `password`).
   - Ролі (`isAdmin`) та внутрішній стан (`points`).

2. **Book:**
   - Повні метадані (назва, автор, серія, жанр, рік).
   - Технічні параметри (шлях до файлу, формат).
   - Медіа-контент (обкладинка, фото автора у вигляді `byte[]`).
   - Гнучка ініціалізація через статичний білдер.

3. **Order:**
   - Зв'язок користувача з конкретним виданням.
   - Статус життєвого циклу замовлення.

---

### Реалізація (Фрагмент Book.java)
```java
package org.example.core.entity;

/**
 * Основна бізнес-сутність книги. 
 * Використовує паттерн Builder для обробки великої кількості метаданих.
 */
public class Book {
    private final String title;
    private final String author;
    private final byte[] cover;
    private Integer databaseId;

    private Book(Builder b) {
        this.title = (b.title == null || b.title.isBlank()) ? "Unknown" : b.title;
        this.author = (b.author == null || b.author.isBlank()) ? "Unknown" : b.author;
        this.cover = b.cover;
        this.databaseId = b.databaseId;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String author;
        private byte[] cover;
        private Integer databaseId;

        public Builder title(String v) { title = v; return this; }
        public Builder author(String v) { author = v; return this; }
        public Builder cover(byte[] v) { cover = v; return this; }
        public Builder databaseId(Integer v) { databaseId = v; return this; }

        public Book build() { return new Book(this); }
    }
}
```

---

### Аналіз та висновки
Розробка сутностей як незалежних POJO-класів дозволила відокремити ядро системи від інфраструктури.
- **Читабельність:** Паттерн Builder робить процес створення об'єктів (особливо при імпорті з метаданих файлів) зрозумілим та безпечним.
- **Ізоляція:** Сутності не залежать від JDBC або сторонніх бібліотек, що відповідає принципам Clean Architecture.
- **Гнучкість:** Система готова до додавання нових полів (наприклад, рейтингу або тегів) без зміни архітектурного підходу.
