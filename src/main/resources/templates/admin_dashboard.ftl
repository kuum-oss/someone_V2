<#import "layout.ftl" as layout>

<@layout.main_layout title="Панель администратора">
    <h1>🛠 Панель администратора</h1>
    
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 2rem;">
        <div class="card">
            <h3>Статистика БД</h3>
            <p><strong>Всего книг:</strong> ${totalBooks}</p>
            <p><strong>Объем данных:</strong> ${totalVolume} GB</p>
        </div>
        <div class="card">
            <h3>Пользователи</h3>
            <p><strong>Всего в системе:</strong> ${users?size}</p>
        </div>
    </div>

    <div class="card" style="margin-bottom: 2rem;">
        <h3>Действия</h3>
        <form action="/admin/add-notification" method="POST" style="display: flex; gap: 10px;">
            <input type="text" name="message" placeholder="Текст уведомления..." required style="flex: 1; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
            <button type="submit" class="btn btn-primary">Разослать всем</button>
        </form>
    </div>

    <div class="card" style="margin-bottom: 2rem;">
        <h3>Список пользователей</h3>
        <table style="width: 100%; border-collapse: collapse;">
            <thead>
                <tr style="border-bottom: 2px solid #eee; text-align: left;">
                    <th style="padding: 10px;">ID</th>
                    <th style="padding: 10px;">Email</th>
                    <th style="padding: 10px;">Баллы</th>
                    <th style="padding: 10px;">Админ</th>
                </tr>
            </thead>
            <tbody>
                <#list users as user>
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 10px;">${user.id}</td>
                        <td style="padding: 10px;">${user.email}</td>
                        <td style="padding: 10px;">${user.points}</td>
                        <td style="padding: 10px;">${user.admin?string("Да", "Нет")}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </div>

    <div class="card">
        <h3>Уведомления</h3>
        <#if notifications?has_content>
            <ul style="list-style: none; padding: 0;">
                <#list notifications as note>
                    <li style="padding: 10px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between;">
                        <span>${note.message}</span>
                        <span style="color: #888; font-size: 0.8rem;">${note.createdAt}</span>
                    </li>
                </#list>
            </ul>
        <#else>
            <p>Уведомлений нет.</p>
        </#if>
    </div>
</@layout.main_layout>
