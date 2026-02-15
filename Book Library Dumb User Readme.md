# Book Library Organizer — README для дебика

Цель: чтобы любой новичок, который полезет в проект, быстро понял архитектуру, слои, тесты и правила.

---

## 1️⃣ Архитектура

Проект построен на **Clean Architecture**:

### Core (Бизнес-логика) — `org.example.core`

* **Entity** — `Book` (данные о книге)
* **UseCases** — сценарии работы с книгами:

    * `ExtractMetadataUseCase`, `OrganizeBooksUseCase`, `GroupBooksUseCase`
* **Ports/Gateways** — интерфейсы к внешнему миру:

    * `MetadataGateway`, `FileGateway`, `ExternalMetadataGateway`
* **Utils** — утилиты для нормализации текста и транслита

### Interface Adapters — `org.example.adapter`

* Преобразует данные между Core и внешним миром
* Адаптеры:

    * `TikaMetadataAdapter` (Tika)
    * `NioFileAdapter` (файловая система)
    * `ThumbnailCacheService` (кэш обложек)

### Infrastructure — `org.example.infrastructure`

* Внешние системы:

    * **UI**: Swing (`BookLibraryGui`, панели деталей книги)
    * **Web**: Javalin + FreeMarker (`WebServer`, шаблоны)
    * **DB**: MySQL (`JdbcBookRepository`, `JdbcUserRepository`)

### Application Root — `org.example`

* `Main.java` — точка входа, выбор режима запуска (GUI/Web)

**Важно:** зависимости всегда внутрь (Infrastructure → Adapters → UseCases → Entities)

---

## 2️⃣ Docker и деплой

* Multi-stage build: Maven → JRE
* docker-compose поднимает App + MySQL
* Healthcheck ждёт готовности БД

---

## 3️⃣ База данных

* `users` — аккаунты, пароли (BCrypt), is_admin, points
* `books` — метаданные и содержимое книги (BLOB)
* Лимит хранилища: 5 ГБ на пользователя

---

## 4️⃣ Веб API

* `/` — главная страница (HTML)
* `/book/{id}/cover` — обложка книги (image/jpeg)
* **Self-healing:**

    * Dynamic Port Binding
    * Database retries (5 попыток, 3 сек задержка)
    * Safe DB Mapping (отсутствие колонок при миграциях)

---

## 5️⃣ Тесты архитектуры

* `ArchitectureGuardTest` проверяет:

    * `externalGateway` не вызывается, если локальных данных достаточно
    * `Book` не мутируется при ошибке в `OrganizeBooksUseCase`
* Комментарии объясняют, **почему тест фиксирует правило**, а не просто метод

---

## 6️⃣ Правила для дебика

1. **Не трогай Core напрямую** без понимания зависимостей
2. **Не убирай тесты**, если они фиксируют архитектурные правила
3. **Логику UI и Web трогать осторожно** — зависит от адаптеров
4. **Документы и комментарии читаемы** — если не понял, перечитай и спроси
5. **Все изменения через UseCases и Gateway**

---

