<#import "layout.ftl" as layout>

<@layout.main_layout title="Модерация отзывов">
    <div class="admin-header" style="margin-bottom: 32px; display: flex; justify-content: space-between; align-items: center;">
        <div>
            <h1 style="font-size: 2rem; font-weight: 800; color: var(--primary-color); margin: 0;">Модерация отзывов</h1>
            <p style="color: var(--secondary-color); margin-top: 4px;">Удаление нежелательного контента</p>
        </div>
        <a href="/admin" class="btn btn-secondary">← В админку</a>
    </div>

    <div class="reviews-list" style="display: flex; flex-direction: column; gap: 16px;">
        <#if reviews?? && reviews?size gt 0>
            <#list reviews as review>
                <div class="review-moderation-card" style="background: white; padding: 24px; border-radius: 12px; border: 1px solid var(--border-color); display: flex; justify-content: space-between; gap: 24px;">
                    <div style="flex: 1;">
                        <div style="display: flex; gap: 12px; align-items: center; margin-bottom: 8px;">
                            <span style="font-weight: 700; color: var(--primary-color);">${review.reviewerName}</span>
                            <span style="color: #94a3b8; font-size: 0.9rem;">о книге</span>
                            <span style="font-weight: 600; color: #1e293b;">${review.bookTitle!"Неизвестная книга"}</span>
                        </div>
                        <p style="font-style: italic; color: #475569; margin: 0; line-height: 1.6;">"${review.reviewText}"</p>
                    </div>
                    <div style="display: flex; align-items: flex-start;">
                        <form action="/admin/reviews/delete" method="post" onsubmit="return confirm('Вы уверены, что хотите удалить этот отзыв?');" style="margin: 0;">
                            <input type="hidden" name="reviewId" value="${review.id?c}">
                            <button type="submit" class="btn btn-danger" style="padding: 10px 16px; background: #fee2e2; color: #ef4444; border: 1px solid #fecaca;">
                                🗑 Удалить
                            </button>
                        </form>
                    </div>
                </div>
            </#list>
        <#else>
            <div style="text-align: center; padding: 60px; background: white; border-radius: 12px; border: 1px solid var(--border-color); color: var(--secondary-color);">
                <p style="font-size: 1.2rem;">Отзывов пока нет.</p>
            </div>
        </#if>
    </div>
</@layout.main_layout>
