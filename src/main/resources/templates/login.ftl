<#import "layout.ftl" as layout>

<@layout.main_layout title="Вхід">
    <div style="max-width: 400px; margin: 5rem auto; font-family: 'Segoe UI', Tahoma, sans-serif;">
        <div class="card" style="
            padding: 2rem;
            border-radius: 12px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.1);
            background-color: #ffffff;
            transition: transform 0.2s;
        "
             onmouseover="this.style.transform='translateY(-5px)';"
             onmouseout="this.style.transform='translateY(0)';"
        >
            <h2 style="text-align:center; margin-bottom: 1.5rem; color: #333;">Вхід до системи</h2>

            <#if error??>
                <div style="
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

            <form action="/login" method="POST">
                <div style="margin-bottom: 1rem;">
                    <label style="display:block; margin-bottom:0.3rem; color:#555;">Email:</label>
                    <input type="email" name="email" required placeholder="example@mail.com" style="
                        width: 100%;
                        padding: 0.6rem 0.8rem;
                        border-radius: 8px;
                        border: 1px solid #ccc;
                        transition: border-color 0.2s;
                    " onfocus="this.style.borderColor='#007BFF';" onblur="this.style.borderColor='#ccc';">
                </div>

                <div style="margin-bottom: 1.2rem;">
                    <label style="display:block; margin-bottom:0.3rem; color:#555;">Пароль:</label>
                    <input type="password" name="password" required style="
                        width: 100%;
                        padding: 0.6rem 0.8rem;
                        border-radius: 8px;
                        border: 1px solid #ccc;
                        transition: border-color 0.2s;
                    " onfocus="this.style.borderColor='#007BFF';" onblur="this.style.borderColor='#ccc';">
                </div>

                <div style="margin-bottom: 1.5rem; display: flex; align-items: center; gap: 8px;">
                    <input type="checkbox" id="rememberMe" style="
                        width: 16px;
                        height: 16px;
                        cursor: pointer;
                        accent-color: #007BFF;
                    ">
                    <label for="rememberMe" style="color: #555; font-size: 0.9rem; cursor: pointer; user-select: none;">
                        Запам'ятати мене та входити автоматично
                    </label>
                </div>

                <button type="submit" style="
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
                    Увійти
                </button>
            </form>

            <script>
            document.addEventListener("DOMContentLoaded", function() {
                const emailInput = document.querySelector('input[name="email"]');
                const passwordInput = document.querySelector('input[name="password"]');
                const rememberCheckbox = document.getElementById('rememberMe');
                const form = document.querySelector('form');
                
                const hasError = <#if error??>true<#else>false</#if>;
                
                if (hasError) {
                    localStorage.removeItem('pendingLogin');
                    localStorage.setItem('autoLogin', 'false');
                }
                
                const savedAccountStr = localStorage.getItem('savedAccount');
                if (savedAccountStr) {
                    try {
                        const savedAccount = JSON.parse(savedAccountStr);
                        if (savedAccount && savedAccount.email) {
                            emailInput.value = savedAccount.email;
                            if (savedAccount.password) {
                                passwordInput.value = savedAccount.password;
                            }
                            rememberCheckbox.checked = true;
                            
                            const autoLogin = localStorage.getItem('autoLogin') === 'true';
                            if (autoLogin && !hasError) {
                                const loadingOverlay = document.createElement('div');
                                loadingOverlay.style.position = 'fixed';
                                loadingOverlay.style.top = '0';
                                loadingOverlay.style.left = '0';
                                loadingOverlay.style.width = '100%';
                                loadingOverlay.style.height = '100%';
                                loadingOverlay.style.backgroundColor = 'rgba(255, 255, 255, 0.8)';
                                loadingOverlay.style.display = 'flex';
                                loadingOverlay.style.flexDirection = 'column';
                                loadingOverlay.style.justifyContent = 'center';
                                loadingOverlay.style.alignItems = 'center';
                                loadingOverlay.style.zIndex = '9999';
                                loadingOverlay.style.fontFamily = "'Inter', sans-serif";
                                loadingOverlay.innerHTML = `
                                    <div style="border: 4px solid #f3f3f3; border-top: 4px solid #007BFF; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin-bottom: 1rem;"></div>
                                    <div style="color: #333; font-weight: 500;">Автоматичний вхід...</div>
                                    <style>
                                        @keyframes spin {
                                            0% { transform: rotate(0deg); }
                                            100% { transform: rotate(360deg); }
                                        }
                                    </style>
                                `;
                                document.body.appendChild(loadingOverlay);
                                form.submit();
                            }
                        }
                    } catch (e) {
                        console.error("Error parsing saved account", e);
                    }
                }
                
                form.addEventListener('submit', function(e) {
                    if (rememberCheckbox.checked) {
                        const pendingLogin = {
                            email: emailInput.value,
                            password: passwordInput.value
                        };
                        localStorage.setItem('pendingLogin', JSON.stringify(pendingLogin));
                        localStorage.setItem('autoLogin', 'true');
                    } else {
                        localStorage.removeItem('savedAccount');
                        localStorage.removeItem('pendingLogin');
                        localStorage.setItem('autoLogin', 'false');
                    }
                });
            });
            </script>

            <p style="text-align: center; margin-top: 1.5rem; color: #666;">
                Немає акаунта? <a href="/register" style="color:#007BFF; text-decoration:none;">Зареєструватися</a>
            </p>
        </div>
    </div>
</@layout.main_layout>