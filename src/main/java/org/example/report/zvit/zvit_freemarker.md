# ТЕХНІЧНИЙ ЗВІТ: ШАБЛОНІЗАЦІЯ ТА ВІДОБРАЖЕННЯ ДАНИХ
## Проект "Smart Organizer"

### Загальна інформація
**Тема:** Використання FreeMarker для генерації динамічного контенту.
**Розробник:** [Ваше Ім'я]
**Дата:** 02.02.2026

---

### Опис етапу розробки
**Мета:** Відокремлення логіки обробки даних від логіки їх відображення за допомогою шаблонізатора FreeMarker.
**Виконані завдання:**
1. Конфігурація FreeMarker Template Engine у проекті.
2. Реалізація механізму формування універсальної моделі даних (`Map<String, Object>`).
3. Розробка шаблонів сторінок з використанням директив FTL (FreeMarker Template Language).
4. Забезпечення безпечного виведення даних (Auto-escaping).

---

### Механізм передачі даних
Процес рендерингу сторінок включає наступні кроки:
1. **Збір контексту:** Веб-сервер формує модель, додаючи до неї об'єкти з бази даних (книги, користувач) та атрибути сесії.
2. **Обробка шаблону:** Двигун FreeMarker зчитує `.ftl` файл та підставляє значення з моделі.
3. **Динамічна генерація:** Використання директив `<#list>` для ітерації по списках книг та `<#if>` для перевірки прав доступу (наприклад, відображення кнопки "Адмінка").

---

### Реалізація (Фрагмент WebServer.java)
```java
// Ініціалізація конфігурації FreeMarker
this.freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_32);
this.freeMarkerCfg.setClassForTemplateLoading(WebServer.class, "/");
this.freeMarkerCfg.setDefaultEncoding("UTF-8");

// Універсальний метод рендерингу
private void render(Context ctx, String templatePath, Map<String, Object> model) {
    try {
        Template template = freeMarkerCfg.getTemplate(templatePath);
        StringWriter writer = new StringWriter();
        // Злиття моделі з шаблоном
        template.process(model, writer);
        ctx.contentType("text/html").result(writer.toString());
    } catch (Exception e) {
        ctx.status(500).result("Помилка генерації контенту");
    }
}
```

---

### Приклад шаблону (library.ftl)
```html
<div class="user-info">
    <span>Вітаємо, ${currentUser.email!"Гість"}!</span>
</div>

<div class="book-list">
    <#list books as book>
        <div class="book-item">
            <h3>${book.title}</h3>
            <p>Жанр: ${book.genre}</p>
        </div>
    </#list>
</div>
```

---

### Аналіз та висновки
Використання FreeMarker дозволило зробити інтерфейс системи гнучким та легким у підтримці.
- **Separation of Concerns:** Дизайнер може працювати над HTML/CSS кодом шаблонів, не торкаючись Java-логіки.
- **Безпека:** Автоматичне екранування символів захищає користувачів від XSS-атак при виведенні метаданих книг.
- **Масштабованість:** Додавання нових полів у модель даних не потребує зміни структури шаблонів, якщо вони не використовуються.
