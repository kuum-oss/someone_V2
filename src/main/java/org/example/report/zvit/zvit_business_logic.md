# ТЕХНІЧНИЙ ЗВІТ: РЕАЛІЗАЦІЯ БІЗНЕС-ЛОГІКИ ТА СЕРВІСНОГО ШАРУ
## Проект "Smart Organizer"

### Загальна інформація
**Тема:** Проектування Use Cases та інтеграція сервісів.
**Розробник:** [Ваше Ім'я]
**Дата:** 02.02.2026

---

### Опис етапу розробки
**Мета:** Централізація алгоритмів управління даними та забезпечення взаємодії між ядром системи та її інтерфейсами.
**Виконані завдання:**
1. Розробка сервісів `AuthService`, `OrderService`, `AdminService`, `FileStorageService`.
2. Реалізація бізнес-правил (наприклад, перевірка лімітів сховища або валідація типів книг при замовленні).
3. Інтеграція сервісів у GUI та Web компоненти.
4. Організація системи сповіщень через `AdminDashboardService`.

---

### Архітектурний підхід
Сервісний шар проекту виконує роль посередника (Facade):
- **Інкапсуляція:** Контролери не знають про деталі роботи з БД або файловою системою.
- **Повторне використання:** Одна і та ж логіка авторизації та управління книгами використовується як у десктопній версії, так і у веб-інтерфейсі.
- **Валідація:** Всі перевірки безпеки та бізнес-обмежень виконуються на рівні сервісів.

---

### Реалізація (Фрагмент OrderService.java)
```java
package org.example.core.service;

import org.example.core.entity.Order;
import org.example.core.entity.StoredBook;
import java.time.LocalDateTime;

/**
 * Керує процесом замовлення фізичних видань.
 */
public class OrderService {
    public Order placeOrder(Integer userId, Integer bookId) {
        StoredBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        // Бізнес-правило: тільки фізичні книги підлягають замовленню
        if (book.getBookType() != StoredBook.BookType.PHYSICAL) {
            throw new IllegalArgumentException("Only physical books can be ordered");
        }
        
        Order order = new Order(null, userId, bookId, Order.Status.PENDING, LocalDateTime.now());
        dashboardService.addNotification(null, "New order: " + book.getTitle());
        
        return orderRepository.save(order);
    }
}
```

---

### Аналіз та висновки
Впровадження структурованого сервісного шару дозволило зробити систему цілісною та безпечною.
- **Чиста архітектура:** Завдяки Clean Architecture, бізнес-логіка ізольована та легко тестується.
- **Масштабованість:** Додавання нових бізнес-правил (наприклад, системи лояльності) потребує змін лише у відповідному сервісі.
- **Надійність:** Централізована обробка виключень та валідація запобігають некоректному стану системи.
