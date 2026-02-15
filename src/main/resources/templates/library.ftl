<#import "layout.ftl" as layout>

<@layout.main_layout title="Моя Библиотека">
    <div class="shop-header" style="margin-bottom: 2rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem;">
            <h1>📚 Моя Библиотека</h1>
            <div class="stats" style="color: var(--secondary-color); font-size: 14px;">
                Книг в библиотеке: ${books?size}
            </div>
        </div>
        
        <div class="filters-container" style="background: #fff; padding: 1.5rem; border-radius: 12px; border: 1px solid var(--border-color); display: flex; flex-direction: column; gap: 1rem;">
            <div class="filter-group" style="flex: 2; min-width: 200px;">
                <label for="searchInput" style="display: block; font-size: 12px; color: var(--secondary-color); margin-bottom: 4px;">Поиск</label>
                <input type="text" id="searchInput" onkeyup="filterBooks()" placeholder="Название или автор..." class="btn btn-secondary" style="width: 100%; text-align: left; padding: 8px 12px; height: auto; box-sizing: border-box;">
            </div>
        </div>
    </div>

    <div class="catalog" id="bookList">
        <#if books?has_content>
            <#list books as book>
                <div class="book-card-wrapper" style="position: relative; display: flex;" data-title="${book.title?lower_case}" data-author="${(book.author!"")?lower_case}">
                    <a href="/book/${book.id?c}" class="book-card" style="flex: 1;">
                        <div class="book-cover">
                            <#if book.cover??>
                                <img src="/book/${book.id?c}/cover" alt="${book.title}">
                            <#else>
                                Нет обложки
                            </#if>
                        </div>

                        <span class="badge ${book.bookType?lower_case}">${book.bookType}</span>

                        <h3 class="book-title">${book.title}</h3>
                        <p class="book-author">${book.author!"Автор не указан"}</p>
                        <div style="font-size: 12px; color: var(--secondary-color); margin-top: 4px;">
                            <span>${book.genre!"Без жанра"}</span> • <span>${book.language!"Неизвестен"}</span>
                        </div>

                        <div style="margin-top: 10px; display: flex; flex-direction: column; gap: 5px;">
                            <#if book.bookType == "ELECTRONIC">
                                <object><a href="/book/${book.id?c}/download" class="cta">Скачать</a></object>
                            <#else>
                                <button class="cta" style="background: var(--success-color); cursor: default;" disabled>Заказано</button>
                            </#if>
                        </div>
                    </a>
                </div>
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
            var cards = document.getElementsByClassName('book-card-wrapper');

            for (var i = 0; i < cards.length; i++) {
                var title = cards[i].getAttribute('data-title');
                var author = cards[i].getAttribute('data-author');
                if (title.indexOf(filter) > -1 || author.indexOf(filter) > -1) {
                    cards[i].style.display = "flex";
                } else {
                    cards[i].style.display = "none";
                }
            }
        }
    </script>
</@layout.main_layout>
