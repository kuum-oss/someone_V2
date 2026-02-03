<#import "layout.ftl" as layout>

<@layout.main_layout title="Магазин">
    <h1>🛒 Магазин книг</h1>
    <p>Здесь вы можете приобрести книги за поинты. Каждая книга стоит 1 поинт (имитация).</p>
    
    <div class="catalog">
        <#if books?has_content>
            <#list books as book>
                <div class="book-card">
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
                    
                    <div class="price-row">
                        <#if book.bookType == "ELECTRONIC">
                            <div class="price">1 поинт</div>
                        <#else>
                            <div class="price free">Бесплатно</div>
                        </#if>
                        <div class="status">Доступна</div>
                    </div>

                    <div style="margin-top: 10px;">
                        <#if currentUser??>
                            <form action="/shop/buy" method="POST" style="margin: 0;">
                                <input type="hidden" name="bookId" value="${book.id?c}">
                                <button type="submit" class="cta">
                                    <#if book.bookType == "PHYSICAL">Заказать<#else>Купить</#if>
                                </button>
                            </form>
                        <#else>
                            <a href="/login" class="cta">Войдите, чтобы <#if book.bookType == "PHYSICAL">заказать<#else>купить</#if></a>
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
