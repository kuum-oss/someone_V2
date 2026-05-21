# Техническая документация API — someone

Данный документ описывает эндпоинты веб-сервера и внутренние механизмы взаимодействия.

## Веб-интерфейс (Javalin)
При запуске в режиме "Сайт (Web)", приложение поднимает сервер на порту `8080` (или другом свободном).

### 1. Главная страница библиотеки (Моя библиотека)
Отображает список книг пользователя. Если пользователь не авторизован, перенаправляет в магазин.
- **URL**: `/`
- **Метод**: `GET`
- **Параметры запроса**: `category` (all, favorite, read, unread)
- **Ответ**: HTML

### 2. Магазин
Список публичных книг для покупки.
- **URL**: `/shop`
- **Метод**: `GET`
- **Параметры запроса**: `q` (поиск), `genre`, `language`, `sort`, `page`
- **Ответ**: HTML

### 3. Детали книги
- **URL**: `/book/{id}`
- **Метод**: `GET`
- **Ответ**: HTML

### 4. Получение медиа-файлов
- **Обложка**: `GET /book/{id}/cover` (image/jpeg)
- **Фото автора**: `GET /book/{id}/author-photo` (image/jpeg)
- **Файл книги**: `GET /book/{id}/download` (application/octet-stream) — требуется владение книгой.

### 5. Читалка и прогресс
- **Интерфейс**: `GET /reader/{id}` (HTML)
- **Обновление прогресса**: `POST /api/reading/progress` (bookId, currentPage, totalPages, speed)
- **Заметки**: `POST /api/reading/notes` (bookId, notes)
- **Отзывы**: `POST /api/reading/review` (bookId, review)

### 6. Покупка и заказы
- **Покупка цифровой книги**: `POST /shop/buy` (bookId)
- **Выбор места (для физических)**: `GET /book/{id}/order` (HTML)
- **Оформление заказа**: `POST /shop/buy/physical` (bookId, seatNumber, hour, duration)

### 7. Аутентификация
- **Вход**: `GET/POST /login`
- **Регистрация**: `GET/POST /register`
- **Выход**: `GET /logout`

## Панель администратора
Доступна пользователям с флагом `is_admin`.

### 1. Общий дашборд
- **URL**: `/admin` (GET)
- **Функции**: Статистика, список пользователей, уведомления, заказы.

### 2. Управление пользователями
- **Профиль**: `GET /admin/user/{id}`
- **Баллы**: `POST /admin/user/update-points` (userId, points)
- **Удаление**: `POST /admin/user/delete` (userId)

### 3. Управление контентом
- **Редактирование книги**: `GET/POST /admin/book/edit`
- **Удаление книги**: `POST /admin/book/delete`
- **Модерация отзывов**: `GET /admin/reviews`

## База данных (MySQL)
Приложение использует MySQL для хранения всех данных. Ниже описаны основные таблицы:

### Таблица `books`
Хранит информацию о книгах, их метаданные и содержимое.
- `id`: Первичный ключ.
- `user_id`: Ссылка на владельца.
- `title`, `author`, `genre`, `year`, `series`: Метаданные.
- `cover`: MEDIUMBLOB (изображение).
- `file_content`: LONGBLOB (бинарные данные файла).

### Таблица `users`
- `id`, `email`, `password` (BCrypt).
- `is_admin`: Флаг администратора.
- `points`: Баланс пользователя.

### Таблица `orders`
- `id`, `user_id`, `book_id`, `seat_number`.
- `start_time`, `end_time`, `status`.

### Таблица `reading_progress`
- `id`: Первичный ключ.
- `user_id`, `book_id`: Ссылки на пользователя и книгу.
- `current_page`, `total_pages`: Текущая позиция и объем.
- `is_favorite`: Флаг "Избранное".
- `reading_speed`: Скорость чтения.
- `notes`, `review`, `highlights`: Пользовательский контент.
- `settings`: JSON с настройками ридера.
- `last_read`: Временная метка последней активности.

## Самоисцеление (Self-healing)
Механизмы автоматического восстановления:
1. **Dynamic Port Binding**: Если порт `8080` занят, `WebServer` вызывает `app.start(0)`, что заставляет ОС выделить первый свободный порт.
2. **Database Retries**: `DatabaseInitializer` выполняет до 5 попыток подключения к MySQL с задержкой в 3 секунды.
3. **Safe DB Mapping**: `JdbcBookRepository` обрабатывает ситуации отсутствия колонок в таблицах (например, при миграциях на лету).
