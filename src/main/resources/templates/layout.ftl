<#macro main_layout title="Smart Organizer">
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        :root {
            --primary-color: #111827;
            --secondary-color: #6b7280;
            --success-color: #16a34a;
            --danger-color: #dc3545;
            --light-color: #f3f4f6;
            --dark-color: #111827;
            --white: #ffffff;
            --shadow: 0 1px 3px rgba(0,0,0,0.1);
            --border-color: #e5e7eb;
        }
        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f9fafb; margin: 0; padding: 0; color: #111827; }
        header { background-color: var(--dark-color); color: var(--white); padding: 1rem 0; box-shadow: var(--shadow); }
        .nav-container { max-width: 1200px; margin: auto; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
        .logo { font-size: 1.5rem; font-weight: bold; text-decoration: none; color: var(--white); display: flex; align-items: center; gap: 10px; }
        nav ul { list-style: none; display: flex; gap: 20px; margin: 0; padding: 0; }
        nav a { text-decoration: none; color: #9ca3af; transition: color 0.2s; font-size: 14px; font-weight: 500; }
        nav a:hover { color: var(--white); }
        .user-info { display: flex; align-items: center; gap: 15px; font-size: 14px; }
        .points-badge { background: #374151; color: #fff; padding: 4px 10px; border-radius: 999px; font-weight: 500; }
        .container { max-width: 1200px; margin: 2rem auto; padding: 0 20px; min-height: 80vh; }
        footer { text-align: center; padding: 3rem 20px; color: #6b7280; border-top: 1px solid var(--border-color); margin-top: 4rem; font-size: 14px; }

        .btn { display: inline-block; padding: 0.5rem 1rem; border-radius: 6px; text-decoration: none; cursor: pointer; border: none; font-size: 14px; font-weight: 500; transition: all 0.2s; text-align: center; }
        .btn:hover { opacity: 0.9; }
        .btn-primary { background-color: var(--primary-color); color: white; }
        .btn-secondary { background-color: #fff; color: var(--primary-color); border: 1px solid var(--border-color); }
        .btn-secondary:hover { background-color: var(--light-color); }
        .btn-success { background-color: var(--success-color); color: white; }

        /* New Catalog Grid */
        .catalog {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
          gap: 24px;
        }

        /* Book Card */
        .book-card {
          border: 1px solid var(--border-color);
          border-radius: 12px;
          padding: 12px;
          background: #fff;
          display: flex;
          flex-direction: column;
          gap: 8px;
          transition: transform 0.15s, box-shadow 0.15s;
          text-decoration: none;
          color: inherit;
          position: relative;
        }
        .book-card:hover {
          transform: translateY(-4px);
          box-shadow: 0 12px 24px rgba(0,0,0,.08);
        }

        /* Covers & Placeholders */
        .book-cover {
          height: 280px;
          background: linear-gradient(135deg, #e5e7eb, #f3f4f6);
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #6b7280;
          font-size: 14px;
          overflow: hidden;
          margin-bottom: 4px;
        }
        .book-cover img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        /* Typography */
        .book-title {
          font-weight: 600;
          font-size: 16px;
          line-height: 1.3;
          max-height: 2.6em;
          overflow: hidden;
          margin: 4px 0 0;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        .book-author {
          font-size: 14px;
          color: #6b7280;
          margin: 0;
        }

        /* Badges */
        .badge {
          font-size: 11px;
          font-weight: 600;
          padding: 3px 8px;
          border-radius: 999px;
          width: fit-content;
          text-transform: uppercase;
          letter-spacing: 0.025em;
        }
        .badge.electronic { background: #e0f2fe; color: #0369a1; }
        .badge.physical { background: #ecfeff; color: #155e75; }

        /* Price & Status */
        .price-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 4px;
        }
        .price { font-weight: 700; font-size: 15px; }
        .price.free { color: var(--success-color); }
        .status { font-size: 12px; color: #6b7280; }

        /* CTA */
        .cta {
          margin-top: auto;
          text-align: center;
          padding: 10px;
          border-radius: 8px;
          background: #111827;
          color: #fff;
          text-decoration: none;
          font-size: 14px;
          font-weight: 500;
          border: none;
          cursor: pointer;
          width: 100%;
          box-sizing: border-box;
        }
        .cta:hover { background: #1f2937; }
    </style>
</head>
<body>
    <header>
        <div class="nav-container">
            <a href="/" class="logo">📚 Smart Organizer</a>
            <nav>
                <ul>
                    <#if currentUser??>
                        <li><a href="/">Библиотека</a></li>
                        <li><a href="/shop">Магазин</a></li>
                    <#else>
                        <li><a href="/shop">Магазин</a></li>
                    </#if>
                    <#if currentUser?? && currentUser.admin>
                        <li><a href="/admin">Админка</a></li>
                    </#if>
                </ul>
            </nav>
            <div class="user-info">
                <#if currentUser??>
                    <span class="points-badge">💰 ${currentUser.points} поинтов</span>
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
