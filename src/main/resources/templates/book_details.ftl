<#import "layout.ftl" as layout>

<@layout.main_layout title=book.title>
    <div class="book-details-container" style="display: flex; gap: 40px; align-items: flex-start;">
        <div class="book-cover-large" style="flex: 0 0 300px;">
            <#if book.cover??>
                <img src="/book/${book.id?c}/cover" alt="${book.title}" style="width: 100%; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.2);">
            <#else>
                <img src="https://via.placeholder.com/300x400?text=No+Cover" alt="No Cover" style="width: 100%; border-radius: 8px;">
            </#if>
        </div>
        
        <div class="book-info-main" style="flex: 1;">
            <h1 style="margin-top: 0;">${book.title}</h1>
            <p style="font-size: 1.2rem; color: #666; margin-bottom: 2rem;">${book.author!"Неизвестный автор"}</p>
            
            <div class="card" style="margin-bottom: 2rem;">
                <table style="width: 100%; border-collapse: collapse;">
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 10px 0; color: #888;">Жанр:</td>
                        <td style="padding: 10px 0;">${book.genre!"-"}</td>
                    </tr>
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 10px 0; color: #888;">Год издания:</td>
                        <td style="padding: 10px 0;">${book.year!"-"}</td>
                    </tr>
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 10px 0; color: #888;">Язык:</td>
                        <td style="padding: 10px 0;">${book.language!"-"}</td>
                    </tr>
                    <#if book.series??>
                        <tr style="border-bottom: 1px solid #eee;">
                            <td style="padding: 10px 0; color: #888;">Серия:</td>
                            <td style="padding: 10px 0;">${book.series} <#if book.seriesIndex??>#${book.seriesIndex}</#if></td>
                        </tr>
                    </#if>
                    <tr>
                        <td style="padding: 10px 0; color: #888;">Тип:</td>
                        <td style="padding: 10px 0;">${book.bookType}</td>
                    </tr>
                </table>
            </div>

            <h3>Описание</h3>
            <div class="description" style="line-height: 1.6; color: #444; margin-bottom: 2rem;">
                ${book.description!"Описание отсутствует."}
            </div>

            <div class="actions" style="display: flex; gap: 10px;">
                <#if isOwned?? && isOwned>
                    <#if book.bookType == "ELECTRONIC">
                        <a href="/book/${book.id?c}/download" class="btn btn-primary">Скачать книгу</a>
                    <#else>
                        <button class="btn btn-secondary" disabled>Книга заказана</button>
                    </#if>
                <#else>
                    <#if currentUser??>
                        <form action="/shop/buy" method="post" style="margin: 0;">
                            <input type="hidden" name="bookId" value="${book.id?c}">
                            <button type="submit" class="btn btn-primary">
                                <#if book.bookType == "PHYSICAL">Заказать книгу<#else>Купить за 1 Pt</#if>
                            </button>
                        </form>
                    <#else>
                        <a href="/login" class="btn btn-secondary">Войдите, чтобы <#if book.bookType == "PHYSICAL">заказать<#else>купить</#if></a>
                    </#if>
                </#if>
                <a href="https://www.youtube.com/results?search_query=обзор+книги+${book.title?url}" target="_blank" class="btn btn-secondary">🎥 Найти обзор</a>
            </div>
        </div>
    </div>
</@layout.main_layout>
