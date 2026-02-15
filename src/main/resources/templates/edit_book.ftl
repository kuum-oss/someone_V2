<#import "layout.ftl" as layout>

<@layout.main_layout title="Редактирование книги">
    <h1>📝 Редактирование книги: ${book.title}</h1>
    
    <div class="card" style="max-width: 800px; margin: 0 auto; padding: 2rem; background: #fff; border-radius: 12px; border: 1px solid var(--border-color);">
        <form action="/admin/book/edit" method="POST" enctype="multipart/form-data" style="display: flex; flex-direction: column; gap: 1.5rem;">
            <input type="hidden" name="id" value="${book.id?c}">
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <div class="form-group">
                    <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Название</label>
                    <input type="text" name="title" value="${book.title}" required style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div class="form-group">
                    <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Автор</label>
                    <input type="text" name="author" value="${book.author!""}" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div class="form-group">
                    <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Жанр</label>
                    <input type="text" name="genre" value="${book.genre!""}" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div class="form-group">
                    <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Язык</label>
                    <input type="text" name="language" value="${book.language!""}" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div class="form-group">
                    <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Год</label>
                    <input type="text" name="year" value="${book.year!""}" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div class="form-group">
                    <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Тип книги</label>
                    <select name="bookType" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">
                        <option value="ELECTRONIC" <#if book.bookType == "ELECTRONIC">selected</#if>>Электронная</option>
                        <option value="PHYSICAL" <#if book.bookType == "PHYSICAL">selected</#if>>Физическая</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Описание</label>
                <textarea name="description" rows="4" style="width: 100%; padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px;">${book.description!""}</textarea>
            </div>

            <div class="form-group" style="padding: 1rem; background: #f8f9fa; border-radius: 8px;">
                <label style="display: block; font-weight: 600; margin-bottom: 0.5rem;">Обложка (JPG)</label>
                <div style="display: flex; gap: 1rem; align-items: center;">
                    <div style="width: 100px; height: 140px; background: #eee; border-radius: 4px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                        <#if book.cover??>
                            <img src="/book/${book.id?c}/cover" style="width: 100%; height: 100%; object-fit: cover;">
                        <#else>
                            <span style="font-size: 10px; color: #888;">Нет обложки</span>
                        </#if>
                    </div>
                    <div style="flex: 1;">
                        <input type="file" name="coverFile" accept="image/jpeg,image/jpg" style="width: 100%;">
                        <p style="font-size: 12px; color: #666; margin-top: 5px;">Выберите новый JPG файл, чтобы заменить текущую обложку.</p>
                    </div>
                </div>
            </div>

            <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 1rem;">
                <a href="/shop" class="btn btn-secondary">Отмена</a>
                <button type="submit" class="btn btn-primary" style="padding: 0.5rem 2rem;">Сохранить изменения</button>
            </div>
        </form>
    </div>
</@layout.main_layout>
