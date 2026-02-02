<#import "layout.ftl" as layout>

<@layout.main_layout title="Магазин">
    <h1>🛒 Магазин книг</h1>
    <p>Здесь вы можете приобрести книги за поинты. Каждая книга стоит 1 поинт (имитация).</p>
    
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
                    <p><strong>Цена:</strong> 1 Pt</p>
                    <p><strong>Тип:</strong> ${book.bookType}</p>
                    <div style="margin-top: 10px;">
                        <#if currentUser??>
                            <form action="/shop/buy" method="POST">
                                <input type="hidden" name="bookId" value="${book.id?c}">
                                <button type="submit" class="btn btn-success" style="width: 100%; background-color: var(--success-color); color: white;">Купить</button>
                            </form>
                        <#else>
                            <a href="/login" class="btn btn-secondary" style="width: 100%;">Войдите, чтобы купить</a>
                        </#if>
                    </div>
                </div>
            </#list>
        <#else>
            <div class="empty" style="grid-column: 1 / -1; text-align: center; padding: 4rem; color: #999;">
                <p>В магазине пока нет книг.</p>
            </div>
        </#if>
    </div>
    
    <#if currentUser??>
        <div style="margin-top: 3rem; text-align: center; padding: 2rem; background: #e9ecef; border-radius: 8px;">
            <h3>Нужно больше поинтов?</h3>
            <form action="/user/add-point" method="POST">
                <button type="submit" class="btn btn-primary">+1 Point (Тест)</button>
            </form>
        </div>
    </#if>
</@layout.main_layout>
