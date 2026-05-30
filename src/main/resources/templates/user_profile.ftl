<#import "layout.ftl" as layout>

<@layout.main_layout title="Профіль користувача">
    <style>
        .profile-container { max-width: 800px; margin: 0 auto; padding-top: 40px; }
        .user-card-header {
            background: white; border-radius: 20px; padding: 40px;
            display: flex; align-items: center; gap: 30px;
            border: 1px solid var(--border-color); box-shadow: var(--shadow-sm); margin-bottom: 30px;
        }
        .user-avatar-circle {
            width: 100px; height: 100px; background: #f1f5f9; border-radius: 50%;
            display: flex; align-items: center; justify-content: center; color: var(--primary-color);
        }
        .user-info-main h2 { margin: 0; font-size: 1.8rem; color: #1e293b; }
        .actions-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .action-box { background: white; padding: 24px; border-radius: 16px; border: 1px solid var(--border-color); }
        .points-display { font-size: 2.5rem; font-weight: 800; color: #059669; margin: 10px 0; }
    </style>

    <div class="profile-container">
        <#if currentUser.admin>
            <a href="/admin" style="display: inline-block; margin-bottom: 20px; text-decoration: none; color: var(--secondary-color);">← Назад в адмінку</a>
        <#else>
            <a href="/" style="display: inline-block; margin-bottom: 20px; text-decoration: none; color: var(--secondary-color);">← До бібліотеки</a>
        </#if>

        <div class="user-card-header">
            <div class="user-avatar-circle">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                </svg>
            </div>
            <div class="user-info-main">
                <span style="font-size: 0.9rem; color: #94a3b8; background: #f8fafc; padding: 2px 8px; border-radius: 4px;">ID #${targetUser.id}</span>
                <h2>${targetUser.email}</h2>
                <p>Статус: <b><#if targetUser.admin>Адміністратор<#else>Користувач</#if></b></p>
            </div>
        </div>

        <div class="actions-grid">
            <div class="action-box">
                <h4>Ваш баланс</h4>
                <div class="points-display">💰 ${targetUser.points}</div>

                <#if currentUser.admin>
                <#-- Форму бачить тільки адмін -->
                    <form action="/admin/user/update-points" method="POST" style="display: flex; gap: 8px;">
                        <input type="hidden" name="userId" value="${targetUser.id?c}">
                        <input type="number" name="points" value="${targetUser.points}" class="admin-input" style="width: 80px; padding: 5px;">
                        <button type="submit" class="btn btn-primary">Змінити</button>
                    </form>
                </#if>
            </div>

            <#if currentUser.admin && currentUser.id != targetUser.id>
                <div class="action-box" style="border-color: #fecaca;">
                    <h4 style="color: #dc2626;">Керування</h4>
                    <p style="font-size: 0.85rem; color: #64748b; margin-bottom: 15px;">Видалення аккаунту не можна скасувати.</p>
                    <form action="/admin/user/delete" method="POST" onsubmit="return confirm('Ви впевнені?');">
                        <input type="hidden" name="userId" value="${targetUser.id?c}">
                        <button type="submit" class="btn btn-secondary" style="color: #dc2626; border-color: #fecaca; width: 100%;">Видалити користувача</button>
                    </form>
                </div>
            </#if>
        </div>

        <#if userOrders?has_content>
            <div class="admin-section" style="margin-top: 30px; background: white; border-radius: 16px; border: 1px solid var(--border-color); overflow: hidden;">
                <div class="section-header" style="padding: 20px; border-bottom: 1px solid var(--border-color); background: #fafafa;">
                    <h3 style="margin: 0;">📦 Замовлення на фізичні книги</h3>
                </div>
                <div class="admin-table-wrapper">
                    <table class="admin-table" style="width: 100%; border-collapse: collapse;">
                        <thead>
                        <tr style="text-align: left; background: #fff; border-bottom: 2px solid var(--border-color);">
                            <th style="padding: 16px;">Книга</th>
                            <th style="padding: 16px;">Місце / Час</th>
                            <th style="padding: 16px;">Статус</th>
                            <th style="padding: 16px;">Дія</th>
                        </tr>
                        </thead>
                        <tbody>
                        <#list userOrders as order>
                            <tr style="border-bottom: 1px solid var(--border-color);">
                                <td style="padding: 16px;">${order.bookTitle}</td>
                                <td style="padding: 16px;">
                                    <#if order.seatNumber??>
                                        💺 ${order.seatNumber}<br>
                                        <small style="color: #64748b;">🕒 ${order.startTime} - ${order.endTime}</small>
                                    <#else>
                                        -
                                    </#if>
                                </td>
                                <td style="padding: 16px;">
                                        <span class="role-badge role-${order.status?lower_case}" style="padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600;
                                                <#if order.status == 'PENDING'>background: #fef3c7; color: #92400e;
                                        <#elseif order.status == 'DELIVERED'>background: #d1fae5; color: #065f46;
                                        <#elseif order.status == 'CANCELLED'>background: #fee2e2; color: #991b1b;
                                        </#if>">
                                            ${order.status}
                                        </span>
                                </td>
                                <td style="padding: 16px;">
                                    <#if order.status == "PENDING">
                                        <form action="/user/order/cancel" method="POST" onsubmit="return confirm('Скасувати замовлення?');">
                                            <input type="hidden" name="orderId" value="${order.id?c}">
                                            <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px; color: #dc2626; border-color: #fecaca;">Скасувати</button>
                                        </form>
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </#if>
    </div>
</@layout.main_layout>