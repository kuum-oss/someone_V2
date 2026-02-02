# Contributing to Book Library Organizer

We welcome contributions! Here's how you can help:

## How to Contribute
1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Write tests for your changes.
4. Ensure all tests pass using `mvn test`.
5. Submit a pull request.

## Code Style
- Follow standard Java naming conventions.
- Use 4 spaces for indentation.
- Run `google-java-format` if possible.

## Тестирование
- Используйте `mvn test` для запуска юнит-тестов.
- Для проверки работы в Docker используйте `docker-compose up --build`.
- При добавлении новых функций в Веб-интерфейс, проверяйте корректность рендеринга шаблонов FreeMarker.

## Локализация
Если вы добавляете новый текст в интерфейс, обязательно обновите файлы:
- `src/main/resources/messages_en.properties`
- `src/main/resources/messages_ru.properties`
- `src/main/resources/messages_uk.properties`
