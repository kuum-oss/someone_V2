<#import "layout.ftl" as layout>

<@layout.main_layout title="Панель управления">
    <style>
        /* 1. Заголовок и общая структура */
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

        /* 2. Виджеты статистики */
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

        /* 3. Секции контента */
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

        /* 4. Стили таблицы пользователей */
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

        /* Контейнер для аватара и ссылки */
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

        /* Бейджи ролей */
        .role-badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
        }
        .role-admin { background: #fee2e2; color: #dc2626; }
        .role-user { background: #f1f5f9; color: #475569; }

        /* 5. Список уведомлений */
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

        @media (max-width: 600px) {
            .notification-form { flex-direction: column; }
        }
    </style>

    <div class="admin-header">
        <h1><span>🛠</span> Панель администратора</h1>
    </div>

    <div class="stats-grid">
        <div class="stat-card">
            <span class="label">Книжный фонд</span>
            <span class="value">${totalBooks}</span>
        </div>
        <div class="stat-card">
            <span class="label">Занято памяти</span>
            <span class="value">${totalVolume} GB</span>
        </div>
        <div class="stat-card">
            <span class="label">Пользователей</span>
            <span class="value">${users?size}</span>
        </div>
        <a href="/admin/reviews" class="stat-card" style="text-decoration: none; transition: transform 0.2s;">
            <span class="label">Модерация</span>
            <span class="value">Отзывы 💬</span>
        </a>
    </div>

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

    <div class="admin-section">
        <div class="section-header">
            <h3>📢 Рассылка уведомлений</h3>
        </div>
        <div class="section-body">
            <form action="/admin/add-notification" method="POST" class="notification-form">
                <input type="text" name="message" class="admin-input" placeholder="Введите текст сообщения для всех пользователей..." required>
                <button type="submit" class="btn btn-primary" style="padding: 12px 24px;">Отправить</button>
            </form>
        </div>
    </div>

    <div class="admin-section">
        <div class="section-header">
            <h3>📦 Управление заказами</h3>
        </div>
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Пользователь</th>
                    <th>Книга</th>
                    <th>Место/Время</th>
                    <th>Статус</th>
                    <th>Действия</th>
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
                                        <button type="submit" class="btn btn-primary" style="padding: 4px 8px; font-size: 12px;">Выполнить</button>
                                    </form>
                                    <form action="/admin/order/update-status" method="POST">
                                        <input type="hidden" name="orderId" value="${order.id?c}">
                                        <input type="hidden" name="status" value="CANCELLED">
                                        <button type="submit" class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px; color: #dc2626; border-color: #fecaca;">Отменить</button>
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

    <div class="admin-section">
        <div class="section-header">
            <h3>👥 Управление пользователями</h3>
        </div>
        <div class="admin-table-wrapper">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Пользователь</th>
                    <th>Баланс баллов</th>
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
                                <span class="role-badge role-admin">Админ</span>
                            <#else>
                                <span class="role-badge role-user">Пользователь</span>
                            </#if>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>

    <div class="admin-section">
        <div class="section-header">
            <h3>🔔 Последние уведомления</h3>
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
                <p style="color: #94a3b8; text-align: center; padding: 20px;">Уведомлений пока нет.</p>
            </#if>
        </div>
    </div>
</@layout.main_layout>