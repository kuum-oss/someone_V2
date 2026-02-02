<#import "layout.ftl" as layout>

<@layout.main_layout title="Моя Библиотека">
    <div class="header-section" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <h1>📚 Моя Библиотека</h1>
        <div class="search-box">
            <input type="text" id="searchInput" onkeyup="filterBooks()" placeholder="Поиск книг..." style="padding: 0.5rem; border-radius: 4px; border: 1px solid #ddd; width: 300px;">
        </div>
    </div>

    <div class="book-list" id="bookList">
        <#if books?has_content>
            <#list books as book>
                <a href="/book/${book.id?c}" class="book-card" data-title="${book.title?lower_case}" data-author="${(book.author!"")?lower_case}">
                    <#if book.cover??>
                        <img src="/book/${book.id?c}/cover" alt="${book.title}">
                    <#else>
                        <img src="https://via.placeholder.com/200x250?text=No+Cover" alt="No Cover">
                    </#if>
                    <h3>${book.title}</h3>
                    <p><strong>Автор:</strong> ${book.author!"Неизвестен"}</p>
                    <p><strong>Жанр:</strong> ${book.genre!"Общий"}</p>
                    <p><strong>Год:</strong> ${book.year!"-"}</p>
                </a>
            </#list>
        <#else>
            <div class="empty" style="grid-column: 1 / -1; text-align: center; padding: 4rem; color: #999;">
                <p>Ваша библиотека пуста. Приобретите книги в <a href="/shop">магазине</a> или добавьте их через приложение.</p>
            </div>
        </#if>
    </div>
    <script>
        function filterBooks() {
            var input = document.getElementById('searchInput');
            var filter = input.value.toLowerCase();
            var cards = document.getElementsByClassName('book-card');

            for (var i = 0; i < cards.length; i++) {
                var title = cards[i].getAttribute('data-title');
                var author = cards[i].getAttribute('data-author');
                if (title.indexOf(filter) > -1 || author.indexOf(filter) > -1) {
                    cards[i].style.display = "";
                } else {
                    cards[i].style.display = "none";
                }
            }
        }
    </script>
</@layout.main_layout>
