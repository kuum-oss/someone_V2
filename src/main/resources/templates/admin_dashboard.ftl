<#import "layout.ftl" as layout>

<@layout.main_layout title="Панель керування">
    <style>
        /* 1. Заголовок та загальна структура */
        .admin-header {
            margin-bottom: 32px;
        }
        .admin-header h1 {
            font-size: 2rem;
            font-weight: 800;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        /* 2. Віджети статистики */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 24px;
            margin-bottom: 40px;
        }
        .stat-card {
            background: white;
            padding: 24px;
            border-radius: 16px;
            border: 1px solid var(--border-color);
            box-shadow: var(--shadow-sm);
            display: flex;
            flex-direction: column;
        }
        .stat-card .label {
            font-size: 0.85rem;
            color: var(--secondary-color);
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 600;
        }
        .stat-card .value {
            font-size: 2rem;
            font-weight: 800;
            color: var(--primary-color);
            margin-top: 8px;
        }

        /* 3. Секції контенту */
        .admin-section {
            background: white;
            border-radius: 16px;
            border: 1px solid var(--border-color);
            margin-bottom: 32px;
            overflow: hidden;
        }
        .section-header {
            padding: 20px 24px;
            border-bottom: 1px solid var(--border-color);
            background: #fafafa;
        }
        .section-header h3 { margin: 0; font-size: 1.1rem; }
        .section-body { padding: 24px; }

        .notification-form {
            display: flex;
            gap: 12px;
        }
        .admin-input {
            flex: 1;
            padding: 12px 16px;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            font-family: inherit;
            font-size: 14px;
            transition: border-color 0.2s;
        }
        .admin-input:focus {
            outline: none;
            border-color: var(--primary-color);
        }

        /* 4. Стилі таблиці користувачів */
        .admin-table-wrapper {
            overflow-x: auto;
        }
        .admin-table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }
        .admin-table th {
            padding: 16px 24px;
            font-size: 0.85rem;
            color: var(--secondary-color);
            font-weight: 600;
            border-bottom: 2px solid var(--border-color);
            background: #fff;
        }
        .admin-table td {
            padding: 16px 24px;
            border-bottom: 1px solid var(--border-color);
            font-size: 14px;
            vertical-align: middle;
        }
        .admin-table tr:hover { background: #f8fafc; }

        /* Контейнер для аватара та посилання */
        .user-info-cell {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .user-avatar-mini {
            width: 32px;
            height: 32px;
            background: #f1f5f9;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #64748b;
            flex-shrink: 0;
        }
        .user-link {
            font-weight: 600;
            text-decoration: none;
            color: var(--primary-color);
        }
        .user-link:hover {
            text-decoration: underline;
        }

        /* Бейджі ролей */
        .role-badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
        }
        .role-admin { background: #fee2e2; color: #dc2626; }
        .role-user { background: #f1f5f9; color: #475569; }

        /* 5. Список сповіщень */
        .note-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px 0;
            border-bottom: 1px solid #f1f5f9;
        }
        .note-item:last-child { border: none; }
        .note-message { font-weight: 500; color: #1e293b; }
        .note-date { font-size: 12px; color: #94a3b8; }

        /* 6. Категорії (вкладки) */
        .admin-nav {
            display: flex;
            gap: 8px;
            margin-bottom: 32px;
            background: white;
            padding: 8px;
            border-radius: 12px;
            border: 1px solid var(--border-color);
            overflow-x: auto;
        }
        .admin-nav-item {
            padding: 10px 20px;
            border-radius: 8px;
            text-decoration: none;
            color: var(--secondary-color);
            font-weight: 600;
            font-size: 14px;
            white-space: nowrap;
            transition: all 0.2s;
        }
        .admin-nav-item:hover {
            background: #f1f5f9;
            color: var(--primary-color);
        }
        .admin-nav-item.active {
            background: var(--primary-gradient);
            color: white;
        }

        /* 7. Відгуки */
        .review-card {
            padding: 20px;
            border-bottom: 1px solid var(--border-color);
        }
        .review-card:last-child { border-bottom: none; }
        .review-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
        .review-book { font-weight: 700; color: var(--primary-color); font-size: 1.1rem; }
        .review-author { font-size: 0.9rem; color: var(--secondary-color); }
        .review-text { color: #334155; line-height: 1.6; font-style: italic; background: #f8fafc; padding: 12px; border-radius: 8px; border-left: 4px solid var(--border-color); }

        @media (max-width: 600px) {
            .notification-form { flex-direction: column; }
        }
    </style>

    <div class="admin-header">
        <h1><span>🛠</span> Панель адміністратора</h1>
    </div>

    <div class="admin-nav">
        <a href="/admin?category=overview" class="admin-nav-item ${ (activeCategory == 'overview')?string('active', '') }">📊 Огляд</a>
        <a href="/admin?category=orders" class="admin-nav-item ${ (activeCategory == 'orders')?string('active', '') }">📦 Замовлення</a>
        <a href="/admin?category=users" class="admin-nav-item ${ (activeCategory == 'users')?string('active', '') }">👥 Користувачі</a>
        <a href="/admin?category=reviews" class="admin-nav-item ${ (activeCategory == 'reviews')?string('active', '') }">💬 Відгуки</a>
        <a href="/admin?category=settings" class="admin-nav-item ${ (activeCategory == 'settings')?string('active', '') }">⚙️ Налаштування</a>
    </div>

    <#if activeCategory == "overview">
        <div class="stats-grid">
            <div class="stat-card">
                <span class="label">Книжковий фонд</span>
                <span class="value">${totalBooks}</span>
            </div>
            <div class="stat-card">
                <span class="label">Зайнято пам'яті</span>
                <span class="value">${totalVolume} GB</span>
            </div>
            <div class="stat-card">
                <span class="label">Користувачів</span>
                <span class="value">${users?size}</span>
            </div>
            <div class="stat-card">
                <span class="label">Відгуки</span>
                <span class="value">${reviews?size}</span>
            </div>
        </div>

        <div class="admin-section">
            <div class="section-header">
                <h3>📢 Розсилка сповіщень</h3>
            </div>
            <div class="section-body">
                <form action="/admin/add-notification" method="POST" class="notification-form">
                    <input type="text" name="message" class="admin-input" placeholder="Введіть текст повідомлення для всіх користувачів..." required>
                    <button type="submit" class="btn btn-primary" style="padding: 12px 24px;">Надіслати</button>
                </form>
            </div>
        </div>

        <div class="admin-section">
            <div class="section-header">
                <h3>🔔 Останні сповіщення</h3>
            </div>
            <div class="section-body">
                <#if notifications?has_content>
                    <div class="notifications-feed">
                        <#list notifications as note>
                            <div class="note-item">
                                <span class="note-message">${note.message}</span>
                                <span class="note-date">🕒 ${note.createdAt}</span>
                            </div>
                        </#list>
                    </div>
                <#else>
                    <p style="color: #94a3b8; text-align: center; padding: 20px;">Сповіщень поки немає.</p>
                </#if>
            </div>
        </div>
    </#if>

    <#if activeCategory == "settings">
        <div class="admin-section">
            <div class="section-header">
                <h3>⚙️ Налаштування бібліотеки</h3>
            </div>
            <div class="section-body">
                <form action="/admin/library-settings" method="POST" style="display: grid; gap: 1rem; max-width: 400px;">
                    <div>
                        <label style="display: block; font-size: 14px; margin-bottom: 4px;">Кількість місць:</label>
                        <input type="number" name="totalSeats" class="admin-input" value="${librarySettings.totalSeats}" required>
                    </div>
                    <div>
                        <label style="display: block; font-size: 14px; margin-bottom: 4px;">Період за замовчуванням (год):</label>
                        <input type="number" name="defaultDuration" class="admin-input" value="${librarySettings.defaultDurationHours}" required>
                    </div>
                    <div>
                        <label style="display: block; font-size: 14px; margin-bottom: 4px;">Доступні періоди (через кому):</label>
                        <input type="text" name="availablePeriods" class="admin-input" value="${librarySettings.availablePeriods}" required>
                    </div>
                    <button type="submit" class="btn btn-primary">Зберегти налаштування</button>
                </form>
            </div>
        </div>
    </#if>

    <#if activeCategory == "orders">
        <div class="admin-section">
            <div class="section-header" style="display: flex; justify-content: space-between; align-items: center;">
                <h3>📦 Керування замовленнями</h3>
                <a href="/admin/scan-qr" class="btn btn-primary" style="padding: 6px 12px; font-size: 14px; text-decoration: none;">📷 Сканувати QR / Номер</a>
            </div>
            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Користувач</th>
                        <th>Книга</th>
                        <th>Місце/Час</th>
                        <th>Статус</th>
                        <th>Дії</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list allOrders as order>
                        <tr>
                            <td style="color: #94a3b8;">#${order.id}</td>
                            <td>${order.userEmail}</td>
                            <td>${order.bookTitle}</td>
                            <td>
                                <#if order.seatNumber??>
                                    💺 ${order.seatNumber}<br>
                                    <small>🕒 ${order.startTime} - ${order.endTime}</small>
                                <#else>
                                    -
                                </#if>
                            </td>
                            <td>
                                <span class="role-badge role-${order.status?lower_case}">${order.status}</span>
                            </td>
                            <td>
                                <div style="display: flex; gap: 4px;">
                                    <#if order.status == "PENDING">
                                        <form action="/admin/order/update-status" method="POST">
                                            <input type="hidden" name="orderId" value="${order.id?c}">
                                            <input type="hidden" name="status" value="DELIVERED">
                                            <button type="submit" class="btn btn-primary" style="padding: 4px 8px; font-size: 12px;">Виконати</button>
                                        </form>
                                        <form action="/admin/order/update-status" method="POST">
                                            <input type="hidden" name="orderId" value="${order.id?c}">
                                            <input type="hidden" name="status" value="CANCELLED">
                                            <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px; color: #dc2626; border-color: #fecaca;">Скасувати</button>
                                        </form>
                                    </#if>
                                </div>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </#if>

    <#if activeCategory == "users">
        <div class="admin-section">
            <div class="section-header">
                <h3>👥 Керування користувачами</h3>
            </div>
            <div class="admin-table-wrapper">
                <table class="admin-table">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Користувач</th>
                        <th>Баланс балів</th>
                        <th>Роль</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#list users as user>
                        <tr>
                            <td style="color: #94a3b8;">#${user.id}</td>
                            <td>
                                <div class="user-info-cell">
                                    <div class="user-avatar-mini">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                            <circle cx="12" cy="7" r="4"></circle>
                                        </svg>
                                    </div>
                                    <a href="/admin/user/${user.id}" class="user-link">${user.email}</a>
                                </div>
                            </td>
                            <td>
                            <span style="display: inline-flex; align-items: center; gap: 4px; font-weight: 600;">
                                💰 ${user.points}
                            </span>
                            </td>
                            <td>
                                <#if user.admin>
                                    <span class="role-badge role-admin">Адмін</span>
                                <#else>
                                    <span class="role-badge role-user">Користувач</span>
                                </#if>
                            </td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </#if>

    <#if activeCategory == "reviews">
        <div class="admin-section">
            <div class="section-header">
                <h3>💬 Модерація відгуків</h3>
            </div>
            <div class="section-body" style="padding: 0;">
                <#if reviews?has_content>
                    <#list reviews as review>
                        <div class="review-card">
                            <div class="review-header">
                                <div>
                                    <div class="review-book">${review.bookTitle}</div>
                                    <div class="review-author">Від: <strong>${review.reviewerName}</strong></div>
                                </div>
                                <form action="/admin/reviews/delete" method="POST" onsubmit="return confirm('Видалити цей відгук?');">
                                    <input type="hidden" name="reviewId" value="${review.id?c}">
                                    <button type="submit" class="btn btn-secondary" style="color: var(--danger-color); padding: 8px;">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <polyline points="3 6 5 6 21 6"></polyline>
                                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                            <line x1="10" y1="11" x2="10" y2="17"></line>
                                            <line x1="14" y1="11" x2="14" y2="17"></line>
                                        </svg>
                                    </button>
                                </form>
                            </div>
                            <div class="review-text">
                                ${review.reviewText}
                            </div>
                        </div>
                    </#list>
                <#else>
                    <p style="color: #94a3b8; text-align: center; padding: 40px;">Відгуків поки немає.</p>
                </#if>
            </div>
        </div>
    </#if>
</@layout.main_layout>