<!DOCTYPE html>
<html lang="ru">
<head>
    <title>${book.title} - Чтение онлайн</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Merriweather:wght@400;700&family=Lora:ital,wght@0,400..700;1,400..700&family=Montserrat:wght@400;600&family=PT+Serif:ital,wght@0,400;0,700;1,400;1,700&display=swap" rel="stylesheet">
    <style>
        :root {
            --reader-bg: #fdfdfd;
            --reader-text: #1a1a1a;
            --sidebar-bg: #ffffff;
            --border-color: #e2e8f0;
            --accent-color: #16a34a;
            --reading-width: 800px;
            --line-height: 1.8;
            --font-family: 'Merriweather', serif;
        }

        body.dark-mode {
            --reader-bg: #121212;
            --reader-text: #e0e0e0;
            --sidebar-bg: #1e1e1e;
            --border-color: #333333;
        }

        body.sepia-mode {
            --reader-bg: #f4ecd8;
            --reader-text: #5b4636;
            --sidebar-bg: #e8dfc8;
            --border-color: #d3c9b0;
            --accent-color: #8f5b3e;
        }

        body {
            margin: 0;
            padding: 0;
            font-family: 'Inter', sans-serif;
            background-color: var(--reader-bg);
            color: var(--reader-text);
            display: flex;
            height: 100vh;
            overflow: hidden;
            transition: background-color 0.3s, color 0.3s;
        }

        /* --- Sidebar --- */
        .sidebar {
            width: 320px;
            background: var(--sidebar-bg);
            border-right: 1px solid var(--border-color);
            display: flex;
            flex-direction: column;
            transition: transform 0.3s ease, width 0.3s ease;
            z-index: 100;
            position: relative;
        }

        .sidebar.closed {
            width: 0;
            overflow: hidden;
            border-right: none;
        }

        .sidebar-header {
            padding: 1.5rem;
            border-bottom: 1px solid var(--border-color);
        }

        .book-info {
            display: flex;
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .book-info img {
            width: 50px;
            height: 75px;
            object-fit: cover;
            border-radius: 4px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .book-meta h1 {
            font-size: 0.9rem;
            margin: 0;
            font-weight: 700;
            line-height: 1.2;
        }

        .book-meta p {
            font-size: 0.75rem;
            color: #64748b;
            margin: 0.25rem 0 0 0;
        }

        .sidebar-tabs {
            display: flex;
            border-bottom: 1px solid var(--border-color);
        }

        .tab-btn {
            flex: 1;
            padding: 0.75rem;
            background: none;
            border: none;
            color: #64748b;
            font-size: 0.8rem;
            font-weight: 600;
            cursor: pointer;
            border-bottom: 2px solid transparent;
        }

        .tab-btn.active {
            color: var(--accent-color);
            border-bottom-color: var(--accent-color);
        }

        .sidebar-content {
            flex: 1;
            overflow-y: auto;
            padding: 1rem;
        }

        .tab-content {
            display: none;
        }

        .tab-content.active {
            display: block;
        }

        .section-title {
            font-size: 0.7rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: #64748b;
            margin-bottom: 0.75rem;
            display: block;
        }

        textarea {
            width: 100%;
            height: 120px;
            background: var(--reader-bg);
            border: 1px solid var(--border-color);
            color: var(--reader-text);
            border-radius: 8px;
            padding: 0.75rem;
            font-family: inherit;
            font-size: 0.85rem;
            resize: none;
            margin-bottom: 0.75rem;
        }

        .btn {
            padding: 0.5rem 1rem;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.8rem;
            cursor: pointer;
            border: none;
            transition: all 0.2s;
            width: 100%;
            margin-bottom: 0.5rem;
        }

        .btn-primary { background: var(--accent-color); color: white; }
        .btn-primary:hover { opacity: 0.9; }

        /* --- Settings Panel --- */
        .settings-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 1rem;
        }

        .setting-item {
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
        }

        .setting-label {
            font-size: 0.8rem;
            font-weight: 600;
        }

        .setting-controls {
            display: flex;
            gap: 0.5rem;
        }

        .setting-btn {
            flex: 1;
            padding: 0.4rem;
            background: var(--reader-bg);
            border: 1px solid var(--border-color);
            color: var(--reader-text);
            border-radius: 4px;
            font-size: 0.75rem;
            cursor: pointer;
        }

        .setting-btn.active {
            background: var(--accent-color);
            color: white;
            border-color: var(--accent-color);
        }

        /* --- Reader Main Area --- */
        .reader-main {
            flex: 1;
            display: flex;
            flex-direction: column;
            position: relative;
            width: 100%;
            transition: margin 0.3s;
        }

        .reader-toolbar {
            height: 50px;
            border-bottom: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 1rem;
            background: var(--sidebar-bg);
            z-index: 10;
        }

        .reader-content {
            flex: 1;
            overflow-y: auto;
            padding: 2rem 1rem;
            display: flex;
            justify-content: center;
            scroll-behavior: smooth;
        }

        .reading-area {
            max-width: var(--reading-width);
            width: 100%;
            font-family: var(--font-family);
            font-size: 1.25rem;
            line-height: var(--line-height);
            white-space: pre-wrap;
            position: relative;
            padding-bottom: 50vh;
            text-align: justify;
            hyphens: auto;
            word-wrap: break-word;
        }

        .reading-area p {
            margin-bottom: 1.5rem;
        }

        .reader-footer {
            height: 50px;
            border-top: 1px solid var(--border-color);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0 1.5rem;
            background: var(--sidebar-bg);
            gap: 1rem;
        }

        .progress-container {
            flex: 1;
            max-width: 300px;
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .progress-bar {
            flex: 1;
            height: 4px;
            background: var(--border-color);
            border-radius: 2px;
            overflow: hidden;
        }

        .progress-fill {
            height: 100%;
            background: var(--accent-color);
            width: 0%;
            transition: width 0.3s;
        }

        .controls {
            display: flex;
            gap: 0.5rem;
        }

        .control-btn {
            background: none;
            border: 1px solid var(--border-color);
            color: var(--reader-text);
            padding: 0.4rem;
            border-radius: 4px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.9rem;
            min-width: 32px;
        }

        .control-btn:hover {
            background: var(--border-color);
        }

        .toc-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .toc-item {
            padding: 0.5rem;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.85rem;
            border-bottom: 1px solid var(--border-color);
        }

        .toc-item:hover {
            background: var(--border-color);
        }

        .highlight-popup {
            position: absolute;
            background: var(--sidebar-bg);
            border: 1px solid var(--border-color);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            border-radius: 8px;
            padding: 0.5rem;
            display: none;
            z-index: 1000;
            flex-direction: row;
            gap: 0.5rem;
        }

        .highlight-yellow { background: #fef08a; color: #854d0e; }
        .highlight-green { background: #bbf7d0; color: #166534; }
        .highlight-blue { background: #bfdbfe; color: #1e40af; }
        
        mark {
            cursor: pointer;
            border-radius: 2px;
        }

        @media (max-width: 768px) {
            .sidebar {
                position: absolute;
                left: 0;
                top: 0;
                bottom: 0;
                transform: translateX(-100%);
            }
            .sidebar.open {
                transform: translateX(0);
                width: 280px;
            }
        }
    </style>
</head>
<body>
    <div class="sidebar" id="sidebar">
        <div class="sidebar-header">
            <div class="book-info">
                <img src="/book/${book.id?c}/cover" alt="Cover">
                <div class="book-meta">
                    <h1>${book.title}</h1>
                    <p>${book.author}</p>
                </div>
            </div>
            <button class="btn btn-primary" onclick="toggleSidebar()">Закрыть панель</button>
        </div>

        <div class="sidebar-tabs">
            <button class="tab-btn active" onclick="switchTab('toc')">Оглавление</button>
            <button class="tab-btn" onclick="switchTab('settings')">Настройки</button>
            <button class="tab-btn" onclick="switchTab('notes')">Заметки</button>
        </div>

        <div class="sidebar-content">
            <!-- Table of Contents -->
            <div id="tocTab" class="tab-content active">
                <ul class="toc-list" id="tocList">
                    <!-- Chapters populated by JS -->
                </ul>
            </div>

            <!-- Settings -->
            <div id="settingsTab" class="tab-content">
                <div class="settings-grid">
                    <div class="setting-item">
                        <span class="setting-label">Тема</span>
                        <div class="setting-controls">
                            <button class="setting-btn" onclick="setTheme('light')" id="theme-light">Светлая</button>
                            <button class="setting-btn" onclick="setTheme('sepia')" id="theme-sepia">Сепия</button>
                            <button class="setting-btn" onclick="setTheme('dark')" id="theme-dark">Темная</button>
                        </div>
                    </div>
                    <div class="setting-item">
                        <span class="setting-label">Шрифт</span>
                        <div class="setting-controls" style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem;">
                            <button class="setting-btn" onclick="setFont('Merriweather', true)" id="font-serif">Merriweather</button>
                            <button class="setting-btn" onclick="setFont('Inter', false)" id="font-sans">Inter</button>
                            <button class="setting-btn" onclick="setFont('Lora', true)" id="font-lora">Lora</button>
                            <button class="setting-btn" onclick="setFont('Montserrat', false)" id="font-montserrat">Montserrat</button>
                            <button class="setting-btn" onclick="setFont('PT Serif', true)" id="font-ptserif">PT Serif</button>
                            <button class="setting-btn" onclick="setFont('OpenDyslexic', false)" id="font-dyslexic">Dyslexic</button>
                        </div>
                    </div>
                    <div class="setting-item">
                        <span class="setting-label">Размер текста</span>
                        <div class="setting-controls">
                            <button class="setting-btn" onclick="changeFontSize(-1)">A-</button>
                            <button class="setting-btn" onclick="changeFontSize(1)">A+</button>
                        </div>
                    </div>
                    <div class="setting-item">
                        <span class="setting-label">Межстрочный интервал</span>
                        <div class="setting-controls">
                            <button class="setting-btn" onclick="setLineHeight(1.4)">Тясный</button>
                            <button class="setting-btn" onclick="setLineHeight(1.8)">Норм</button>
                            <button class="setting-btn" onclick="setLineHeight(2.2)">Широкий</button>
                        </div>
                    </div>
                    <div class="setting-item">
                        <span class="setting-label">Ширина текста</span>
                        <div class="setting-controls">
                            <button class="setting-btn" onclick="setTextWidth('600px')">600</button>
                            <button class="setting-btn" onclick="setTextWidth('800px')">800</button>
                            <button class="setting-btn" onclick="setTextWidth('1000px')">1000</button>
                        </div>
                    </div>
                    <div class="setting-item">
                        <span class="setting-label">Автопрокрутка (Скорость)</span>
                        <div class="setting-controls" style="flex-direction: column;">
                            <button class="setting-btn" onclick="toggleAutoScroll()" id="autoScrollBtn">Выкл</button>
                            <input type="range" min="0.1" max="5" step="0.1" value="1" id="scrollSpeed" style="width: 100%;">
                        </div>
                    </div>
                </div>
            </div>

            <!-- Notes -->
            <div id="notesTab" class="tab-content">
                <span class="section-title">Заметки</span>
                <textarea id="notesArea" placeholder="Ваши мысли о прочитанном...">${(progress.notes)!""}</textarea>
                <button class="btn btn-primary" onclick="saveNotes()">Сохранить заметки</button>

                <div style="margin-top: 1.5rem;">
                    <span class="section-title">Ваш отзыв</span>
                    <textarea id="reviewArea" placeholder="Напишите отзыв..." style="height: 80px;">${(progress.review)!""}</textarea>
                    <button class="btn btn-primary" onclick="saveReview()">Сохранить отзыв</button>
                </div>
            </div>
            
            <div style="margin-top: 1.5rem; padding-bottom: 2rem;">
                <a href="/" style="color: var(--accent-color); text-decoration: none; font-size: 0.85rem; font-weight: 600;">← В библиотеку</a>
            </div>
        </div>
    </div>

    <div class="reader-main">
        <div class="reader-toolbar">
            <button class="control-btn" onclick="toggleSidebar()" title="Меню">☰</button>
            
            <div class="controls">
                <button class="control-btn" onclick="prevPage()" title="Назад (←)">←</button>
                <button class="control-btn" onclick="nextPage()" title="Вперед (→)">→</button>
                <button class="control-btn" onclick="toggleDarkMode()" title="Темная тема">🌓</button>
                <a href="/" class="control-btn" title="Домой" style="text-decoration: none;">🏠</a>
            </div>
        </div>

        <div class="reader-content" id="readerContent">
            <div class="reading-area" id="readingArea"><#if bookContent??>${bookContent}<#else><p style="text-align: center; padding: 4rem;">Загрузка...</p></#if></div>
            
            <div class="highlight-popup" id="highlightPopup">
                <button class="control-btn highlight-yellow" onclick="applyHighlight('yellow')">✏️</button>
                <button class="control-btn highlight-green" onclick="applyHighlight('green')">✏️</button>
                <button class="control-btn highlight-blue" onclick="applyHighlight('blue')">✏️</button>
                <button class="control-btn" onclick="addComment()">💬</button>
            </div>
        </div>

        <div class="reader-footer">
            <div class="progress-container">
                <span id="progressPercent">0%</span>
                <div class="progress-bar">
                    <div class="progress-fill" id="progressFill"></div>
                </div>
                <span id="pageInfo">...</span>
            </div>
        </div>
    </div>

    <script>
        let currentBookId = ${bookId?c};
        let currentPage = ${(progress.currentPage?c)!1};
        let totalPages = 1;
        let startTime = Date.now();
        let pagesReadThisSession = 0;
        let autoScrollInterval = null;
        let highlights = JSON.parse('${(progress.highlights?js_string)!"[]"}');
        let settings = JSON.parse('${(progress.settings?js_string)! "{}" }');

        const readingArea = document.getElementById('readingArea');
        const readerContent = document.getElementById('readerContent');
        const fullContent = readingArea.textContent;
        const wordsPerPage = 300;
        const words = fullContent.split(/\s+/);
        totalPages = Math.ceil(words.length / wordsPerPage) || 1;

        // Populate TOC (naive)
        function initTOC() {
            const tocList = document.getElementById('tocList');
            const chapterCount = Math.min(10, Math.ceil(totalPages / 10)); 
            for (let i = 1; i <= chapterCount; i++) {
                const li = document.createElement('li');
                li.className = 'toc-item';
                li.innerText = 'Глава ' + i;
                li.onclick = () => {
                    currentPage = (i-1) * 10 + 1;
                    if (currentPage > totalPages) currentPage = totalPages;
                    updateDisplay();
                };
                tocList.appendChild(li);
            }
        }

        function updateDisplay() {
            const start = (currentPage - 1) * wordsPerPage;
            const end = start + wordsPerPage;
            readingArea.textContent = words.slice(start, end).join(' ');
            
            // Re-apply highlights if we had precise logic, but here we just update basic page info
            document.getElementById('pageInfo').innerText = currentPage + ' / ' + totalPages;
            const percent = Math.round((currentPage / totalPages) * 100);
            document.getElementById('progressPercent').innerText = percent + '%';
            document.getElementById('progressFill').style.width = percent + '%';
            
            localStorage.setItem('book_' + currentBookId + '_page', currentPage);
            readerContent.scrollTop = 0;
            syncProgress();
        }

        function nextPage() { if (currentPage < totalPages) { currentPage++; pagesReadThisSession++; updateDisplay(); } }
        function prevPage() { if (currentPage > 1) { currentPage--; updateDisplay(); } }

        function switchTab(tabId) {
            document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.getElementById(tabId + 'Tab').classList.add('active');
            event.currentTarget.classList.add('active');
        }

        function setTheme(theme) {
            document.body.classList.remove('dark-mode', 'sepia-mode');
            if (theme !== 'light') document.body.classList.add(theme + '-mode');
            settings.theme = theme;
            saveSettings();
        }

        function setFont(family, isSerif) {
            document.documentElement.style.setProperty('--font-family', family + (isSerif ? ', serif' : ', sans-serif'));
            settings.font = family;
            saveSettings();
        }

        let fontSize = 1.25;
        function changeFontSize(delta) {
            fontSize += delta * 0.1;
            readingArea.style.fontSize = fontSize + 'rem';
            settings.fontSize = fontSize;
            saveSettings();
        }

        function setLineHeight(val) {
            document.documentElement.style.setProperty('--line-height', val);
            settings.lineHeight = val;
            saveSettings();
        }

        function setTextWidth(val) {
            document.documentElement.style.setProperty('--reading-width', val);
            settings.textWidth = val;
            saveSettings();
        }

        function toggleAutoScroll() {
            if (autoScrollInterval) {
                clearInterval(autoScrollInterval);
                autoScrollInterval = null;
                document.getElementById('autoScrollBtn').innerText = 'Выкл';
            } else {
                const speedInput = document.getElementById('scrollSpeed');
                autoScrollInterval = setInterval(() => {
                    const speed = parseFloat(speedInput.value);
                    readerContent.scrollTop += speed;
                    if (readerContent.scrollTop + readerContent.clientHeight >= readerContent.scrollHeight - 10) {
                        nextPage();
                    }
                }, 50); // Fixed interval, variable increment for smoothness
                document.getElementById('autoScrollBtn').innerText = 'Вкл';
            }
        }

        function toggleSidebar() { document.getElementById('sidebar').classList.toggle('closed'); }

        function toggleDarkMode() {
            const isDark = document.body.classList.toggle('dark-mode');
            setTheme(isDark ? 'dark' : 'light');
        }

        function applyHighlight(color) {
            const selection = window.getSelection();
            if (selection.rangeCount > 0) {
                const range = selection.getRangeAt(0);
                const mark = document.createElement('mark');
                mark.className = 'highlight-' + color;
                range.surroundContents(mark);
                
                highlights.push({
                    text: selection.toString(),
                    color: color,
                    page: currentPage,
                    timestamp: Date.now()
                });
                syncHighlights();
            }
            document.getElementById('highlightPopup').style.display = 'none';
        }

        function addComment() {
            const comment = prompt('Введите комментарий:');
            if (comment) {
                const selection = window.getSelection();
                highlights.push({
                    text: selection.toString(),
                    comment: comment,
                    page: currentPage,
                    timestamp: Date.now()
                });
                syncHighlights();
                alert('Комментарий сохранен');
            }
            document.getElementById('highlightPopup').style.display = 'none';
        }

        function syncProgress() {
            const now = Date.now();
            const hoursElapsed = (now - startTime) / (1000 * 60 * 60);
            const speed = hoursElapsed > 0 ? (pagesReadThisSession / hoursElapsed) : 0;
            fetch('/api/reading/progress', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'bookId=' + currentBookId + '&currentPage=' + currentPage + '&totalPages=' + totalPages + '&speed=' + speed
            });
        }

        function saveSettings() {
            fetch('/api/reading/settings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'bookId=' + currentBookId + '&settings=' + encodeURIComponent(JSON.stringify(settings))
            });
        }

        function syncHighlights() {
            fetch('/api/reading/highlights', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'bookId=' + currentBookId + '&highlights=' + encodeURIComponent(JSON.stringify(highlights))
            });
        }

        function saveNotes() {
            const notes = document.getElementById('notesArea').value;
            fetch('/api/reading/notes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'bookId=' + currentBookId + '&notes=' + encodeURIComponent(notes)
            }).then(() => alert('Заметки сохранены'));
        }

        function saveReview() {
            const review = document.getElementById('reviewArea').value;
            fetch('/api/reading/review', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'bookId=' + currentBookId + '&review=' + encodeURIComponent(review)
            }).then(() => alert('Отзыв сохранен'));
        }

        // Keyboard shortcuts
        window.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowRight') nextPage();
            if (e.key === 'ArrowLeft') prevPage();
            if (e.key === '+') changeFontSize(1);
            if (e.key === '-') changeFontSize(-1);
        });

        // Click to flip
        readerContent.onclick = (e) => {
            if (e.target === readerContent || e.target === readingArea) {
                if (e.clientX > window.innerWidth * 0.7) nextPage();
                else if (e.clientX < window.innerWidth * 0.3) prevPage();
            }
        };

        // Selection popup
        readingArea.onmouseup = (e) => {
            const selection = window.getSelection();
            if (selection.toString().length > 0) {
                const popup = document.getElementById('highlightPopup');
                popup.style.display = 'flex';
                popup.style.left = e.pageX + 'px';
                popup.style.top = (e.pageY - 50) + 'px';
            } else {
                document.getElementById('highlightPopup').style.display = 'none';
            }
        };

        // Init settings
        if (settings.theme) setTheme(settings.theme);
        if (settings.fontSize) {
            fontSize = settings.fontSize;
            readingArea.style.fontSize = fontSize + 'rem';
        }
        if (settings.lineHeight) setLineHeight(settings.lineHeight);
        if (settings.textWidth) setTextWidth(settings.textWidth);
        if (settings.font) {
            const serifFonts = ['Merriweather', 'Lora', 'PT Serif'];
            setFont(settings.font, serifFonts.includes(settings.font));
        }

        initTOC();
        updateDisplay();
        setInterval(syncProgress, 30000);
    </script>
</body>
</html>
