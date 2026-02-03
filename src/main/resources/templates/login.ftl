<#import "layout.ftl" as layout>

<@layout.main_layout title="Вход">
    <div style="max-width: 400px; margin: 4rem auto;">
        <h2>Вход в систему</h2>
        <#if error??>
            <div style="color: red; margin-bottom: 1rem;">${error}</div>
        </#if>
        <form action="/login" method="POST">
            <div style="margin-bottom: 1rem; display: flex; align-items: center; gap: 10px;">
                <label style="min-width: 60px;">Email:</label>
                <input type="email" name="email" required placeholder="example@mail.com" style="padding: 4px; border: 1px solid #ccc; border-radius: 4px; flex: 1;">
            </div>
            <div style="margin-bottom: 1rem; display: flex; align-items: center; gap: 10px;">
                <label style="min-width: 60px;">Пароль:</label>
                <input type="password" name="password" required style="padding: 4px; border: 1px solid #ccc; border-radius: 4px; flex: 1;">
            </div>
            <button type="submit" style="width: 100%; padding: 10px; background: #111827; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: 16px;">Войти</button>
        </form>
        <p style="text-align: center; margin-top: 1rem;">
            Нет аккаунта? <a href="/register" style="color: #4338ca;">Зарегистрироваться</a>
        </p>
    </div>
</@layout.main_layout>
