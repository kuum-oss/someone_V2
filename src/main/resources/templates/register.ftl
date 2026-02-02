<#import "layout.ftl" as layout>

<@layout.main_layout title="Регистрация">
    <div style="max-width: 400px; margin: 4rem auto;">
        <div class="card">
            <h2>Регистрация</h2>
            <#if error??>
                <div class="alert alert-error">${error}</div>
            </#if>
            <form action="/register" method="POST">
                <div class="form-group">
                    <label>Email:</label>
                    <input type="email" name="email" required placeholder="example@mail.com">
                </div>
                <div class="form-group">
                    <label>Пароль:</label>
                    <input type="password" name="password" required minlength="4">
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%;">Зарегистрироваться</button>
            </form>
            <p style="text-align: center; margin-top: 1rem;">
                Уже есть аккаунт? <a href="/login">Войти</a>
            </p>
        </div>
    </div>
</@layout.main_layout>
