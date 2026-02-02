# Структура проекта Book Library Organizer

Проект построен на принципах **Чистой архитектуры (Clean Architecture)** Роберта Мартина. Основная идея заключается в разделении ответственности и направлении зависимостей внутрь к бизнес-логике.

## Архитектурные слои

### 1. Core (Бизнес-логика) - `org.example.core`
Это ядро системы, которое не зависит от внешних библиотек, баз данных или интерфейса.

*   **Entity** (`org.example.core.entity`):
    - `Book.java`: Основная бизнес-сущность книги. Инкапсулирует данные о книге (название, автор, путь к файлу, метаданные).
*   **Use Cases** (`org.example.core.usecase`):
    - Описывают специфичные для приложения правила (сценарии использования).
    - `ExtractMetadataUseCase`: Сценарий извлечения метаданных из файла.
    - `OrganizeBooksUseCase`: Сценарий физической организации библиотеки на диске.
    - `GroupBooksUseCase`: Логика группировки книг для отображения в интерфейсе.
*   **Ports (Gateways)** (`org.example.core.usecase.port`):
    - Интерфейсы, которые определяют, какие данные нужны ядру от внешнего мира.
    - `MetadataGateway`: Интерфейс для локального извлечения метаданных.
    - `FileGateway`: Интерфейс для работы с файловой системой.
    - `ExternalMetadataGateway`: Интерфейс для получения данных из сети (API).
*   **Utils** (`org.example.core.util`):
    - `TextNormalizer`: Общая логика обработки текста, очистки имен файлов и нормализации данных.
    - `TransliterationUtils`: Логика детекции и конвертации транслита (Latin -> Cyrillic).

### 2. Interface Adapters - `org.example.adapter`
Слой адаптеров, преобразующих данные из формата, удобного для внешних систем, в формат, удобный для Use Cases.

*   **Gateways** (`org.example.adapter.gateway`):
    - `TikaMetadataAdapter`: Реализация `MetadataGateway` с использованием Apache Tika.
    - `NioFileAdapter`: Реализация `FileGateway` через стандартный Java NIO.
    - `ThumbnailCacheService`: Сервис для кэширования обложек на диске.

### 3. Infrastructure (Внешние системы) - `org.example.infrastructure`
Самый внешний слой: UI, Web, База данных, конфигурация.

*   **UI** (`org.example.infrastructure.ui`):
    - `BookLibraryGui`: Главное окно приложения на Swing.
    - `components/BookDetailsPanel`: Панель с детальной информацией о книге.
    - `GenreImageService`: Логика подбора иконок для жанров.
*   **Web** (`org.example.infrastructure.web`):
    - `WebServer`: Сервер на базе Javalin для отображения библиотеки в браузере.
    - `templates/library.ftl`: Шаблон FreeMarker для генерации HTML.
*   **DB** (`org.example.infrastructure.db`):
    - `DatabaseInitializer`: Скрипты создания таблиц и механизм повторных попыток подключения.
    - `DatabaseConfig`: Конфигурация пула соединений (DBCP2).
*   **Repository** (`org.example.infrastructure.repository`):
    - `JdbcBookRepository`: Реализация доступа к книгам в MySQL.
    - `JdbcUserRepository`: Управление пользователями.

### 4. Application Root - `org.example`
*   **Main.java**: Точка входа. Реализует выбор режима запуска (GUI/Web) и логику автоматического открытия браузера.

## Контейнеризация и Docker
Проект полностью поддерживает Docker:
- **Dockerfile**: Использует Multi-stage build (Maven для сборки, JRE для запуска).
- **docker-compose.yml**: Поднимает связку App + MySQL.
- **Healthcheck**: Приложение ждет готовности базы данных перед запуском.

## База данных и Хранилище

### 1. MySQL (База данных)
Приложение автоматически инициализирует базу данных при старте.

*   **Таблицы:**
    *   `users`: хранит аккаунты пользователей (email, зашифрованный пароль).
    *   `books`: хранит информацию о загруженных файлах (название, путь на сервере, размер).

### 2. Облачное хранилище (MySQL BLOB)
*   **Метод хранения:** Все файлы книг сохраняются непосредственно в базу данных в колонку `file_content` типа `LONGBLOB`.
*   **Изоляция:** Данные каждого пользователя привязаны к его `user_id`.
*   **Лимиты:** На каждого пользователя установлено ограничение в **5 ГБ**. При достижении лимита (проверяется по сумме `file_size`) загрузка новых книг блокируется.
*   **Локальное хранилище:** Папка `storage/` в корне проекта больше не используется для хранения пользовательских данных.

## Основные зависимости и технологии
- **Apache Tika**: Для извлечения метаданных из PDF, EPUB, FB2, MOBI.
- **FlatLaf**: Современная тема оформления для Swing.
- **Jackson**: Обработка JSON ответов от API.
- **SLF4J**: Логирование.

## Правила зависимости
Dependencies point **INWARDS**:
`Infrastructure -> Adapters -> Use Cases -> Entities`
Ни один класс из слоя Core не знает о существовании Swing, Tika или HttpClient.
