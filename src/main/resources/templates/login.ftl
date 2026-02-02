<#import "layout.ftl" as layout>

<@layout.main_layout title="Вход">
    <div style="max-width: 400px; margin: 4rem auto;">
        <div class="card">
            <h2>Вход в систему</h2>
            <#if error??>
                <div class="alert alert-error">${error}</div>
            </#if>
            <form action="/login" method="POST">
                <div class="form-group">
                    <label>Email:</label>
                    <input type="email" name="email" required placeholder="example@mail.com">
                </div>
                <div class="form-group">
                    <label>Пароль:</label>
                    <input type="password" name="password" required>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%;">Войти</button>
            </form>
            <p style="text-align: center; margin-top: 1rem;">
                Нет аккаунта? <a href="/register">Зарегистрироваться</a>
            </p>
        </div>
    </div>
</@layout.main_layout>
