<#macro main_layout title="Smart Organizer">
    <!DOCTYPE html>
    <html lang="uk">
    <head>
        <title>${title}</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
        <style>
            :root {
                --primary-gradient: linear-gradient(135deg, #111827 0%, #374151 100%);
                --accent-gradient: linear-gradient(135deg, #16a34a 0%, #22c55e 100%);
                --primary-color: #13564a;
                --secondary-color: #64748b;
                --success-color: #16a34a;
                --danger-color: #ef4444;
                --bg-color: #f8fafc;
                --card-bg: #ffffff;
                --border-color: #e2e8f0;
                --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
                --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
                --shadow-lg: 0 20px 25px -5px rgb(0 0 0 / 0.1);
                --radius: 12px;
            }

            * { box-sizing: border-box; }

            body {
                font-family: 'Inter', sans-serif;
                background-color: var(--bg-color);
                margin: 0;
                padding: 0;
                color: var(--primary-color);
                line-height: 1.5;
            }

            /* --- Navigation --- */
            header {
                background-color: rgba(17, 24, 39, 0.95);
                backdrop-filter: blur(10px);
                color: white;
                padding: 0.75rem 0;
                position: sticky;
                top: 0;
                z-index: 1000;
                box-shadow: var(--shadow-md);
            }
            .nav-container {
                max-width: 1200px;
                margin: auto;
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 0 20px;
            }
            .logo {
                font-size: 1.25rem;
                font-weight: 700;
                text-decoration: none;
                color: white;
                display: flex;
                align-items: center;
                gap: 8px;
                transition: opacity 0.2s;
            }
            .logo:hover { opacity: 0.8; }

            nav ul { list-style: none; display: flex; gap: 24px; margin: 0; padding: 0; }
            nav a {
                text-decoration: none;
                color: #94a3b8;
                transition: all 0.2s;
                font-size: 14px;
                font-weight: 500;
                position: relative;
                padding: 4px 0;
            }
            nav a:hover { color: white; }
            nav a::after {
                content: '';
                position: absolute;
                bottom: 0; left: 0; width: 0; height: 2px;
                background: var(--success-color);
                transition: width 0.3s;
            }
            nav a:hover::after { width: 100%; }

            .user-info { display: flex; align-items: center; gap: 16px; font-size: 14px; }
            .points-badge {
                background: rgba(255,255,255,0.1);
                border: 1px solid rgba(255,255,255,0.1);
                color: #fff;
                padding: 6px 12px;
                border-radius: 999px;
                font-weight: 600;
                box-shadow: var(--shadow-sm);
            }

            /* --- Layout --- */
            .container {
                max-width: 1200px;
                margin: 2rem auto;
                padding: 0 20px;
                min-height: 80vh;
            }

            /* --- Buttons --- */
            .btn {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                padding: 0.6rem 1.2rem;
                border-radius: 8px;
                text-decoration: none;
                cursor: pointer;
                border: none;
                font-size: 14px;
                font-weight: 600;
                transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            }
            .btn-primary {
                background: var(--primary-gradient);
                color: white;
                box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            }
            .btn-primary:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 15px rgba(0,0,0,0.15);
                filter: brightness(1.1);
            }
            .btn-secondary {
                background: white;
                color: var(--primary-color);
                border: 1px solid var(--border-color);
            }
            .btn-secondary:hover { background: var(--bg-color); border-color: #cbd5e1; }

            /* --- Catalog & Cards --- */
            .catalog {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
                gap: 32px;
                animation: staggerIn 0.6s ease-out;
            }

            .book-card {
                background: var(--card-bg);
                border: 1px solid var(--border-color);
                border-radius: var(--radius);
                padding: 16px;
                display: flex;
                flex-direction: column;
                gap: 12px;
                transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
                text-decoration: none;
                color: inherit;
                height: 100%;
            }

            .book-card:hover {
                transform: translateY(-10px) scale(1.02);
                box-shadow: var(--shadow-lg);
                border-color: transparent;
            }

            .book-cover {
                aspect-ratio: 2/3;
                height: auto;
                background: #f1f5f9;
                border-radius: 8px;
                overflow: hidden;
                position: relative;
            }
            .book-cover img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
            .book-card:hover .book-cover img { transform: scale(1.1); }

            .book-title {
                font-weight: 700;
                font-size: 1.05rem;
                line-height: 1.4;
                margin: 0;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                min-height: 2.8em;
            }

            .book-author { font-size: 0.9rem; color: var(--secondary-color); margin-top: -4px; }

            .badge {
                font-size: 10px;
                font-weight: 700;
                padding: 4px 10px;
                border-radius: 6px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
                width: fit-content;
            }
            .badge.electronic { background: #eff6ff; color: #2563eb; }
            .badge.physical { background: #f0fdf4; color: #16a34a; }

            .price-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-top: auto;
                padding-top: 12px;
                border-top: 1px solid #f1f5f9;
            }
            .price { font-weight: 800; font-size: 1.1rem; color: var(--primary-color); }
            .price.free { color: var(--success-color); }

            .cta {
                background: var(--primary-gradient);
                color: white;
                padding: 12px;
                border-radius: 10px;
                text-align: center;
                font-weight: 600;
                font-size: 14px;
                margin-top: 8px;
                transition: all 0.3s;
            }
            .cta:hover { filter: brightness(1.2); letter-spacing: 0.5px; }

            /* --- Animations --- */
            @keyframes staggerIn {
                from { opacity: 0; transform: translateY(20px); }
                to { opacity: 1; transform: translateY(0); }
            }

            /* --- Responsiveness --- */
            @media (max-width: 768px) {
                .nav-container { flex-direction: column; gap: 15px; padding: 15px; }
                .catalog { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 16px; }
                .book-title { font-size: 0.95rem; -webkit-line-clamp: 2; }
                .user-info { width: 100%; justify-content: center; }
                nav ul { gap: 15px; }
            }

            footer {
                text-align: center;
                padding: 4rem 20px;
                color: var(--secondary-color);
                background: white;
                border-top: 1px solid var(--border-color);
                margin-top: 4rem;
                font-size: 14px;
            }
        </style>
    </head>
    <body>
    <header>
        <div class="nav-container">
            <a href="/" class="logo">
                <span style="font-size: 1.5rem;">📚</span> Smart Organizer
            </a>
            <nav>
                <ul>
                    <#if currentUser??>
                        <li><a href="/">Бібліотека</a></li>
                        <li><a href="/my-reading">Моє читання</a></li>
                        <li><a href="/shop">Магазин</a></li>
                    <#else>
                        <li><a href="/shop">Магазин</a></li>
                    </#if>
                    <#if currentUser?? && currentUser.admin>
                        <li><a href="/admin">Адмін</a></li>
                    </#if>
                </ul>
            </nav>
            <div class="user-info">
                <#if currentUser??>
                    <a href="/admin/user/${currentUser.id?c}" class="user-profile-link" style="text-decoration: none; display: flex; align-items: center; gap: 8px; margin-right: 15px;">
                        <div class="user-avatar-mini" style="width: 28px; height: 28px; background: #f1f5f9; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #64748b;">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                <circle cx="12" cy="7" r="4"></circle>
                            </svg>
                        </div>
                        <span style="font-size: 14px; font-weight: 500; color: var(--primary-color);">${currentUser.email}</span>
                    </a>

                    <span class="points-badge">💰 ${currentUser.points}</span>
                    <a href="/logout" id="logout-btn" class="btn btn-secondary" style="padding: 6px 12px; margin-left: 10px;">Вийти</a>
                <#else>
                    <a href="/login" class="btn btn-primary">Увійти</a>
                </#if>
            </div>
        </div>
    </header>

    <div class="container">
        <#nested>
    </div>

    <footer>
        <p>&copy; 2026 Smart Organizer. Зроблено з любов'ю для читачів.</p>
    </footer>

    <script>
    document.addEventListener("DOMContentLoaded", function() {
        const logoutBtn = document.getElementById("logout-btn");
        if (logoutBtn) {
            logoutBtn.addEventListener("click", function() {
                localStorage.setItem("autoLogin", "false");
            });
        }
        
        <#if currentUser??>
        const pendingLoginStr = localStorage.getItem('pendingLogin');
        if (pendingLoginStr) {
            localStorage.setItem('savedAccount', pendingLoginStr);
            localStorage.removeItem('pendingLogin');
            localStorage.setItem('autoLogin', 'true');
        }
        <#else>
        if (localStorage.getItem('autoLogin') === 'true' && localStorage.getItem('savedAccount')) {
            const path = window.location.pathname;
            if (path !== '/login' && path !== '/register') {
                window.location.href = '/login';
            }
        }
        </#if>
    });
    </script>
    </body>
    </html>
</#macro>