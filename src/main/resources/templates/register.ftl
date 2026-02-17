<#import "layout.ftl" as layout>

<@layout.main_layout title="Регистрация">
    <div style="max-width: 400px; margin: 5rem auto; font-family: 'Segoe UI', Tahoma, sans-serif;">
        <div class="card" style="
            padding: 2rem;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.1);
            background-color: #ffffff;
            transition: transform 0.2s;
        "
             onmouseover="this.style.transform='translateY(-5px)';"
             onmouseout="this.style.transform='translateY(0)';">
            <h2 style="text-align:center; margin-bottom: 1.5rem; color: #333;">Регистрация</h2>

            <#if error??>
                <div class="alert alert-error" style="
                    background-color: #ffe6e6;
                    color: #b30000;
                    padding: 0.8rem;
                    border-radius: 8px;
                    margin-bottom: 1rem;
                    text-align:center;
                ">
                    ${error}
                </div>
            </#if>

            <form action="/register" method="POST">
                <div class="form-group" style="margin-bottom: 1rem;">
                    <label style="display:block; margin-bottom:0.3rem; color:#555;">Email:</label>
                    <input type="email" name="email" required placeholder="example@mail.com" style="
                        width: 100%;
                        padding: 0.6rem 0.8rem;
                        border-radius: 8px;
                        border: 1px solid #ccc;
                        transition: border-color 0.2s;
                    " onfocus="this.style.borderColor='#007BFF';" onblur="this.style.borderColor='#ccc';">
                </div>

                <div class="form-group" style="margin-bottom: 1.5rem;">
                    <label style="display:block; margin-bottom:0.3rem; color:#555;">Пароль:</label>
                    <input type="password" name="password" required minlength="4" style="
                        width: 100%;
                        padding: 0.6rem 0.8rem;
                        border-radius: 8px;
                        border: 1px solid #ccc;
                        transition: border-color 0.2s;
                    " onfocus="this.style.borderColor='#007BFF';" onblur="this.style.borderColor='#ccc';">
                </div>

                <button type="submit" class="btn btn-primary" style="
                    width: 100%;
                    padding: 0.7rem;
                    background-color: #007BFF;
                    border: none;
                    border-radius: 8px;
                    color: white;
                    font-weight: 600;
                    cursor: pointer;
                    transition: background-color 0.2s, transform 0.2s;
                "
                        onmouseover="this.style.backgroundColor='#0056b3'; this.style.transform='scale(1.03)';"
                        onmouseout="this.style.backgroundColor='#007BFF'; this.style.transform='scale(1)';">
                    Зарегистрироваться
                </button>
            </form>

            <p style="text-align: center; margin-top: 1.5rem; color: #666;">
                Уже есть аккаунт? <a href="/login" style="color:#007BFF; text-decoration:none;">Войти</a>
            </p>
        </div>
    </div>
</@layout.main_layout>
