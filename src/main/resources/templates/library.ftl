<!DOCTYPE html>
<html>
<head>
    <title>Моя Библиотека</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f9; margin: 0; padding: 20px; }
        h1 { color: #333; text-align: center; }
        .container { max-width: 1000px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .book-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
        .book-card { border: 1px solid #ddd; padding: 10px; border-radius: 5px; text-align: center; background: #fff; transition: transform 0.2s; }
        .book-card:hover { transform: translateY(-5px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
        .book-card img { max-width: 100%; height: 250px; object-fit: cover; border-radius: 3px; }
        .book-card h3 { margin: 10px 0 5px; font-size: 1.1em; color: #007bff; }
        .book-card p { margin: 0; color: #666; font-size: 0.9em; }
        .empty { text-align: center; color: #999; padding: 40px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>📚 Моя Библиотека</h1>
        <div class="book-list">
            <#if books?has_content>
                <#list books as book>
                    <div class="book-card">
                        <#if book.cover??>
                            <img src="/book/${book.id?c}/cover" alt="${book.title}">
                        <#else>
                            <img src="https://via.placeholder.com/200x250?text=No+Cover" alt="No Cover">
                        </#if>
                        <h3>${book.title}</h3>
                        <p><strong>Автор:</strong> ${book.author!"Неизвестен"}</p>
                        <p><strong>Жанр:</strong> ${book.genre!"Общий"}</p>
                        <p><strong>Год:</strong> ${book.year!"-"}</p>
                    </div>
                </#list>
            <#else>
                <div class="empty">
                    <p>Библиотека пуста. Добавьте книги через приложение.</p>
                </div>
            </#if>
        </div>
    </div>
</body>
</html>
