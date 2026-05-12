<#import "layout.ftl" as layout>

<@layout.main_layout title=book.title>
    <style>
        /* 1. Сетка страницы */
        .book-page-grid {
            display: grid;
            grid-template-columns: 320px 1fr;
            gap: 48px;
            /* Важно: отступ сверху, чтобы контент не нырял под шапку */
            padding-top: 20px;
            align-items: start;
        }

        /* 2. Левая колонка (Обложка) */
        .book-sticky-side {
            position: sticky;
            top: 100px; /* Фиксируем обложку при скролле */
        }

        .book-visual {
            width: 100%;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 20px 50px rgba(0,0,0,0.1);
            background: #fff;
            line-height: 0;
        }

        .book-visual img {
            width: 100%;
            height: auto;
            display: block;
            transition: transform 0.5s ease;
        }

        .book-visual:hover img {
            transform: scale(1.03);
        }

        /* 3. Правая колонка (Инфо) */
        .book-main-info {
            display: flex;
            flex-direction: column;
        }

        .book-header-group {
            margin-bottom: 32px;
            border-bottom: 1px solid var(--border-color);
            padding-bottom: 24px;
        }

        .book-header-group h1 {
            font-size: 2.5rem;
            font-weight: 800;
            margin: 0 0 8px 0;
            color: var(--primary-color);
            line-height: 1.1;
        }

        .book-author-link {
            font-size: 1.25rem;
            color: var(--secondary-color);
            text-decoration: none;
            font-weight: 500;
        }

        /* Фото автора (кружок) */
        .author-photo-container {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .author-circle {
            width: 48px;
            height: 48px;
            border-radius: 50%;
            overflow: hidden;
            border: 2px solid var(--border-color);
            background: #f1f5f9;
            flex-shrink: 0;
        }

        .author-circle img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .author-circle-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2rem;
            color: #94a3b8;
        }

        /* 4. Блок характеристик (вместо таблицы) */
        .specs-list {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            margin-bottom: 32px;
        }

        .spec-tag {
            background: #fff;
            border: 1px solid var(--border-color);
            padding: 8px 16px;
            border-radius: 10px;
            display: flex;
            flex-direction: column;
            min-width: 120px;
        }

        .spec-tag .label {
            font-size: 0.75rem;
            color: #94a3b8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 2px;
        }

        .spec-tag .value {
            font-weight: 600;
            font-size: 0.95rem;
            color: var(--primary-color);
        }

        /* 5. Описание */
        .book-description {
            font-size: 1.1rem;
            line-height: 1.7;
            color: #334155;
            margin-bottom: 40px;
            max-width: 800px;
        }

        /* 6. Кнопки действий */
        .book-actions {
            display: flex;
            gap: 16px;
            background: #fff;
            padding: 20px;
            border-radius: 16px;
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-sm);
            width: fit-content;
        }

        /* Мобильная адаптация */
        @media (max-width: 900px) {
            .book-page-grid {
                grid-template-columns: 1fr;
                gap: 32px;
            }
            .book-sticky-side {
                position: static;
                max-width: 280px;
                margin: 0 auto;
            }
            .book-header-group { text-align: center; }
            .book-actions { width: 100%; flex-direction: column; }
        }
    </style>

    <div class="book-page-grid">
        <aside class="book-sticky-side">
            <div class="book-visual">
                <#if book.cover??>
                    <img src="/book/${book.id?c}/cover" alt="${book.title}">
                <#else>
                    <div style="padding: 100px 20px; text-align: center; color: #94a3b8; font-weight: 600;">
                        📖 Обложка отсутствует
                    </div>
                </#if>
            </div>
        </aside>

        <main class="book-main-info">
            <div class="book-header-group">
                <h1>${book.title}</h1>
                <div class="author-photo-container">
                    <div class="author-circle">
                        <#if book.authorPhoto??>
                            <img src="/book/${book.id?c}/author-photo" alt="${book.author!"Автор"}">
                        <#else>
                            <div class="author-circle-placeholder">👤</div>
                        </#if>
                    </div>
                    <span class="book-author-link">${book.author!"Неизвестный автор"}</span>
                </div>
            </div>

            <div class="specs-list">
                <div class="spec-tag">
                    <span class="label">Жанр</span>
                    <span class="value">${book.genre!"—"}</span>
                </div>
                <div class="spec-tag">
                    <span class="label">Год</span>
                    <span class="value">${book.year!"—"}</span>
                </div>
                <div class="spec-tag">
                    <span class="label">Тип</span>
                    <span class="badge ${book.bookType?lower_case}" style="margin-top: 4px;">${book.bookType}</span>
                </div>
                <#if book.series??>
                    <div class="spec-tag">
                        <span class="label">Серия</span>
                        <span class="value">${book.series} <#if book.seriesIndex??>#${book.seriesIndex}</#if></span>
                    </div>
                </#if>
            </div>

            <article class="book-description">
                <h3 style="margin-bottom: 12px; font-size: 1.25rem;">О чем эта книга</h3>
                ${book.description!"Описание в процессе добавления..."}
            </article>

            <div class="book-actions" style="margin-bottom: 40px;">
                <#if isOwned?? && isOwned>
                    <#if book.bookType == "ELECTRONIC">
                        <div style="display: flex; gap: 12px;">
                            <a href="/book/${book.id?c}/download" class="btn btn-secondary" style="padding: 14px 24px;">📥 Скачать</a>
                            <a href="/reader/${book.id?c}" class="btn btn-primary" style="padding: 14px 28px;">📖 Читать онлайн</a>
                        </div>
                    <#else>
                        <div style="display: flex; flex-direction: column; gap: 8px;">
                            <span class="btn btn-secondary" style="cursor: default; opacity: 0.7;">📦 Книга уже заказана</span>
                            <p style="font-size: 0.85rem; color: #64748b; margin: 0;">Вы сможете заказать её снова после возврата или отмены текущего заказа.</p>
                        </div>
                    </#if>
                <#else>
                    <#if currentUser??>
                        <form action="/shop/buy" method="post" style="margin: 0;">
                            <input type="hidden" name="bookId" value="${book.id?c}">
                            <button type="submit" class="btn btn-primary" style="padding: 14px 28px;">
                                <#if book.bookType == "PHYSICAL">🛒 Заказать за 1 поинт<#else>💎 Купить за 1 поинт</#if>
                            </button>
                        </form>
                    <#else>
                        <a href="/login" class="btn btn-primary" style="padding: 14px 28px;">🔐 Войти для покупки</a>
                    </#if>
                </#if>
                <a href="https://www.youtube.com/results?search_query=обзор+книги+${book.title?url}"
                   target="_blank" class="btn btn-secondary" style="padding: 14px 24px;">🎥 Посмотреть обзор</a>
            </div>

            <#if reviews?? && reviews?size gt 0>
                <section class="book-reviews" style="margin-top: 40px; border-top: 1px solid var(--border-color); padding-top: 32px;">
                    <h3 style="margin-bottom: 24px; font-size: 1.5rem;">Отзывы читателей</h3>
                    <div style="display: flex; flex-direction: column; gap: 20px;">
                        <#list reviews as review>
                            <div class="review-card" style="background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid var(--border-color); position: relative;">
                                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                                    <span style="font-weight: 600; color: var(--primary-color);">${review.reviewerName}</span>
                                    <#if currentUser?? && currentUser.admin>
                                        <form action="/admin/reviews/delete" method="post" onsubmit="return confirm('Вы уверены?');" style="margin: 0;">
                                            <input type="hidden" name="reviewId" value="${review.id?c}">
                                            <input type="hidden" name="bookId" value="${book.id?c}">
                                            <button type="submit" style="background: none; border: none; cursor: pointer; color: #ef4444; font-size: 1.2rem;" title="Удалить отзыв">🗑</button>
                                        </form>
                                    </#if>
                                </div>
                                <p style="font-style: italic; color: #475569; line-height: 1.6; margin: 0;">"${review.reviewText}"</p>
                            </div>
                        </#list>
                    </div>
                </section>
            </#if>
        </main>
    </div>
</@layout.main_layout>