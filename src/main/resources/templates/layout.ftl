<#macro main_layout title="Smart Organizer">
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        :root {
            --primary-color: #007bff;
            --secondary-color: #6c757d;
            --success-color: #28a745;
            --danger-color: #dc3545;
            --light-color: #f8f9fa;
            --dark-color: #343a40;
            --white: #ffffff;
            --shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f9; margin: 0; padding: 0; color: #333; }
        header { background-color: var(--dark-color); color: var(--white); padding: 1rem 0; box-shadow: var(--shadow); }
        .nav-container { max-width: 1200px; margin: auto; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
        .logo { font-size: 1.5rem; font-weight: bold; text-decoration: none; color: var(--white); display: flex; align-items: center; gap: 10px; }
        nav ul { list-style: none; display: flex; gap: 20px; margin: 0; padding: 0; }
        nav a { text-decoration: none; color: #ccc; transition: color 0.2s; }
        nav a:hover { color: var(--white); }
        .user-info { display: flex; align-items: center; gap: 15px; }
        .points-badge { background: var(--success-color); padding: 2px 8px; border-radius: 12px; font-size: 0.8rem; }
        .container { max-width: 1200px; margin: 2rem auto; padding: 0 20px; min-height: 80vh; }
        footer { text-align: center; padding: 2rem; color: #888; border-top: 1px solid #ddd; margin-top: 3rem; }
        .btn { display: inline-block; padding: 0.5rem 1rem; border-radius: 5px; text-decoration: none; cursor: pointer; border: none; font-size: 0.9rem; transition: opacity 0.2s; }
        .btn:hover { opacity: 0.8; }
        .btn-primary { background-color: var(--primary-color); color: white; }
        .btn-secondary { background-color: var(--secondary-color); color: white; }
        .btn-danger { background-color: var(--danger-color); color: white; }
        .card { background: white; padding: 1.5rem; border-radius: 8px; box-shadow: var(--shadow); }
        .form-group { margin-bottom: 1rem; }
        .form-group label { display: block; margin-bottom: 0.5rem; }
        .form-group input { width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        .alert { padding: 1rem; border-radius: 4px; margin-bottom: 1rem; }
        .alert-error { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        
        /* Book styles */
        .book-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
        .book-card { border: 1px solid #ddd; padding: 10px; border-radius: 5px; text-align: center; background: #fff; transition: transform 0.2s; text-decoration: none; color: inherit; }
        .book-card:hover { transform: translateY(-5px); box-shadow: 0 5px 15px rgba(0,0,0,0.1); }
        .book-card img { max-width: 100%; height: 250px; object-fit: cover; border-radius: 3px; }
        .book-card h3 { margin: 10px 0 5px; font-size: 1.1em; color: var(--primary-color); }
        .book-card p { margin: 0; color: #666; font-size: 0.9em; }
    </style>
</head>
<body>
    <header>
        <div class="nav-container">
            <a href="/" class="logo">📚 Smart Organizer</a>
            <nav>
                <ul>
                    <li><a href="/">Библиотека</a></li>
                    <li><a href="/shop">Магазин</a></li>
                    <#if currentUser?? && currentUser.admin>
                        <li><a href="/admin">Админка</a></li>
                    </#if>
                </ul>
            </nav>
            <div class="user-info">
                <#if currentUser??>
                    <span class="points-badge">💰 ${currentUser.points} pts</span>
                    <span>${currentUser.email}</span>
                    <a href="/logout" class="btn btn-secondary">Выйти</a>
                <#else>
                    <a href="/login" class="btn btn-primary">Войти</a>
                    <a href="/register" class="btn btn-secondary">Регистрация</a>
                </#if>
            </div>
        </div>
    </header>

    <div class="container">
        <#nested>
    </div>

    <footer>
        <p>&copy; 2026 Smart Organizer. Сделано с любовью для ваших книг.</p>
    </footer>
</body>
</html>
</#macro>
