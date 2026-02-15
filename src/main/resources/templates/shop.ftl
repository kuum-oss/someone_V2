<#import "layout.ftl" as layout>

<@layout.main_layout title="Магазин">
    <div class="shop-header" style="margin-bottom: 2rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem;">
            <h1>🛒 Магазин книг</h1>
            <div class="stats" style="color: var(--secondary-color); font-size: 14px;">
                Найдено книг: ${totalBooks!0}
            </div>
        </div>
        
        <form action="/shop" method="GET" class="filters-container" style="background: #fff; padding: 1.5rem; border-radius: 12px; border: 1px solid var(--border-color); display: flex; flex-direction: column; gap: 1rem;">
            <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
                <div class="filter-group" style="flex: 2; min-width: 200px;">
                    <label for="q" style="display: block; font-size: 12px; color: var(--secondary-color); margin-bottom: 4px;">Поиск</label>
                    <input type="text" name="q" id="q" value="${query!""}" placeholder="Название или автор..." class="btn btn-secondary" style="width: 100%; text-align: left; padding: 8px 12px; height: auto; box-sizing: border-box;">
                </div>

                <div class="filter-group" style="flex: 1; min-width: 150px;">
                    <label for="genre" style="display: block; font-size: 12px; color: var(--secondary-color); margin-bottom: 4px;">Жанр</label>
                    <select name="genre" id="genre" class="btn btn-secondary" style="width: 100%; padding: 8px 12px; height: auto;">
                        <option value="">Все жанры</option>
                        <#if genres??>
                            <#list genres as g>
                                <option value="${g}" <#if selectedGenre?? && selectedGenre == g>selected</#if>>${g}</option>
                            </#list>
                        </#if>
                    </select>
                </div>
                
                <div class="filter-group" style="flex: 1; min-width: 150px;">
                    <label for="language" style="display: block; font-size: 12px; color: var(--secondary-color); margin-bottom: 4px;">Язык</label>
                    <select name="language" id="language" class="btn btn-secondary" style="width: 100%; padding: 8px 12px; height: auto;">
                        <option value="">Все языки</option>
                        <#if languages??>
                            <#list languages as l>
                                <option value="${l}" <#if selectedLanguage?? && selectedLanguage == l>selected</#if>>${l}</option>
                            </#list>
                        </#if>
                    </select>
                </div>

                <div class="filter-group" style="flex: 1; min-width: 150px;">
                    <label for="sort" style="display: block; font-size: 12px; color: var(--secondary-color); margin-bottom: 4px;">Сортировка</label>
                    <select name="sort" id="sort" class="btn btn-secondary" style="width: 100%; padding: 8px 12px; height: auto;">
                        <option value="title" <#if sort?? && sort == "title">selected</#if>>По названию</option>
                        <option value="author" <#if sort?? && sort == "author">selected</#if>>По автору</option>
                        <option value="newest" <#if sort?? && sort == "newest">selected</#if>>Сначала новые</option>
                    </select>
                </div>
            </div>
            
            <div style="display: flex; gap: 10px; justify-content: flex-end;">
                <a href="/shop" class="btn btn-secondary" style="padding: 8px 20px;">Сброс</a>
                <button type="submit" class="btn btn-primary" style="padding: 8px 20px;">Найти</button>
            </div>
        </form>
    </div>

    <p style="margin-top: -1rem; margin-bottom: 2rem; color: var(--secondary-color);">Здесь вы можете приобрести книги за поинты. Каждая книга стоит 1 поинт (имитация).</p>

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
                    <div style="font-size: 12px; color: var(--secondary-color); margin-top: 4px;">
                        <span>${book.genre!"Без жанра"}</span> • <span>${book.language!"Неизвестен"}</span>
                    </div>

                    <div class="price-row">
                        <#if book.bookType == "ELECTRONIC">
                            <div class="price">1 поинт</div>
                        <#else>
                            <div class="price free">Бесплатно</div>
                        </#if>
                        <div class="status">Доступна</div>
                    </div>

                    <div style="margin-top: 10px;">
                        <#if ownedIds?? && ownedIds?seq_contains(book.id)>
                            <button class="cta" style="background: var(--success-color); cursor: default;" disabled>Уже куплено</button>
                        <#elseif currentUser??>
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

    <#if (totalPages?? && totalPages > 1)>
        <div class="pagination" style="margin-top: 3rem; display: flex; justify-content: center; gap: 5px;">
            <#list 1..totalPages as p>
                <#assign url = "/shop?page=${p}">
                <#if query?? && query != ""><#assign url = url + "&q=${query}"></#if>
                <#if selectedGenre?? && selectedGenre != ""><#assign url = url + "&genre=${selectedGenre}"></#if>
                <#if selectedLanguage?? && selectedLanguage != ""><#assign url = url + "&language=${selectedLanguage}"></#if>
                <#if sort?? && sort != ""><#assign url = url + "&sort=${sort}"></#if>
                
                <a href="${url}" class="btn <#if currentPage == p>btn-primary<#else>btn-secondary</#if>" style="padding: 8px 14px;">
                    ${p}
                </a>
            </#list>
        </div>
    </#if>

    <#if currentUser??>
        <div style="margin-top: 3rem; text-align: center; padding: 2rem; background: #e9ecef; border-radius: 8px;">
            <h3>Нужно больше поинтов?</h3>
            <form action="/user/add-point" method="POST">
                <button type="submit" class="btn btn-primary">+1 Point (Тест)</button>
            </form>
        </div>
    </#if>
</@layout.main_layout>
