<#import "layout.ftl" as layout>
<@layout.main_layout title="Моє читання">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="margin: 0; font-size: 2rem; font-weight: 800; color: #111827;">Моє читання</h1>
            <p style="color: #64748b; margin-top: 0.5rem;">Ваш прогрес і список книг, які ви читаєте</p>
        </div>
    </div>

    <#if readingList?has_content>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 3rem;">
            <#list readingList as progress>
                <div class="card" style="display: flex; flex-direction: column; height: 100%; animation: staggerIn ${(progress_index * 0.1) + 0.2}s ease-out both;">
                    <div style="display: flex; gap: 1rem; padding: 1.25rem;">
                        <img src="/book/${progress.bookId?c}/cover" alt="Cover" style="width: 80px; height: 120px; object-fit: cover; border-radius: 8px; box-shadow: var(--shadow-sm); flex-shrink: 0; background: #f1f5f9;">
                        <div style="flex: 1; min-width: 0;">
                            <h3 style="margin: 0 0 0.25rem 0; font-size: 1.1rem; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                <#list books as b><#if b.id == progress.bookId>${b.title}</#if></#list>
                            </h3>

                            <p style="margin: 0; font-size: 0.9rem; color: #64748b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                <#list books as b><#if b.id == progress.bookId>${b.author}</#if></#list>
                            </p>

                            <div style="margin-top: 1rem;">

                                <div style="display: flex; justify-content: space-between; font-size: 0.8rem; margin-bottom: 0.25rem;">
                                    <span>Прогрес: ${progress.currentPage?c} / ${progress.totalPages?c} стор.</span>
                                    <span>${(progress.currentPage / (progress.totalPages > 0)?then(progress.totalPages, 1) * 100)?string("0")}%</span>

                                </div>
                                <div style="width: 100%; height: 6px; background: #e2e8f0; border-radius: 10px; overflow: hidden;">
                                    <div style="width: ${(progress.currentPage / (progress.totalPages > 0)?then(progress.totalPages, 1) * 100)}%; height: 100%; background: var(--accent-gradient);"></div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <div style="padding: 0 1.25rem 1.25rem 1.25rem; display: flex; flex-direction: column; gap: 0.75rem; margin-top: auto;">
                        <div style="display: flex; gap: 0.5rem; font-size: 0.8rem; color: #64748b;">
                            <span style="background: #f1f5f9; padding: 2px 8px; border-radius: 4px;">Швидкість: ${progress.readingSpeed?string("0.1")} стор/год</span>
                        </div>
                        <a href="/reader/${progress.bookId?c}" class="cta" style="margin-top: 0;">Читати далі</a>
                    </div>
                </div>

            </#list>
        </div>

        <div class="card" style="padding: 2rem; margin-bottom: 2rem;">
            <h2 style="margin-top: 0; margin-bottom: 1.5rem; font-size: 1.5rem;">Статистика читання</h2>
            <div style="height: 300px; width: 100%; display: flex; align-items: flex-end; gap: 1rem; padding-bottom: 1rem;">
                <#-- Simple bar chart representation -->
                <#list readingList as progress>
                    <div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 0.5rem;">
                        <div style="width: 40px; background: var(--accent-gradient); height: ${(progress.currentPage / (progress.totalPages > 0)?then(progress.totalPages, 1) * 200)?string("0")}px; border-radius: 4px 4px 0 0; transition: height 1s ease;"></div>
                        <span style="font-size: 0.7rem; text-align: center; height: 3em; overflow: hidden; display: block;">
                            <#list books as b><#if b.id == progress.bookId>${b.title}</#if></#list>
                        </span>
                    </div>

                </#list>
            </div>
            <div style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #e2e8f0; color: #64748b; font-size: 0.9rem;">
                На графіку відображено прогрес читання (у сторінках) для останніх активних книг.
            </div>
        </div>
    <#else>
        <div class="card" style="padding: 4rem; text-align: center; background: white;">
            <div style="font-size: 4rem; margin-bottom: 1rem;">📖</div>
            <h2 style="margin-top: 0; color: #1e293b;">Список читання порожній</h2>
            <p style="color: #64748b; max-width: 400px; margin: 0 auto 2rem;">Почніть читати книги з вашої бібліотеки, і вони з'являться тут разом із вашим прогресом.</p>
            <a href="/" class="btn btn-primary">Перейти до бібліотеки</a>
        </div>
    </#if>
</@layout.main_layout>