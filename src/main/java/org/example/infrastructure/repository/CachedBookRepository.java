package org.example.infrastructure.repository;

import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.infrastructure.cache.CacheConfig;

import java.util.List;
import java.util.Optional;

public class CachedBookRepository implements BookRepository {
    private final BookRepository delegate;

    public CachedBookRepository(BookRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public StoredBook save(StoredBook book) {
        StoredBook saved = delegate.save(book);
        if (saved.getId() != null) {
            CacheConfig.getBookCache().put(saved.getId(), saved);
        }
        return saved;
    }

    @Override
    public Optional<StoredBook> findById(Integer id) {
        StoredBook cached = CacheConfig.getBookCache().getIfPresent(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<StoredBook> book = delegate.findById(id);
        book.ifPresent(b -> CacheConfig.getBookCache().put(id, b));
        return book;
    }

    @Override
    public void update(StoredBook book) {
        delegate.update(book);
        if (book.getId() != null) {
            // Либо обновляем в кэше, либо инвалидируем. 
            // Инвалидация надежнее, так как update может менять не все поля, 
            // а findById загружает полный объект (кроме контента файла).
            CacheConfig.invalidateBook(book.getId());
        }
    }

    @Override
    public void deleteById(Integer id) {
        delegate.deleteById(id);
        CacheConfig.invalidateBook(id);
    }

    // Остальные методы просто делегируют вызовы, так как они возвращают списки
    // или агрегаты, которые сложнее кэшировать эффективно без сложной инвалидации.

    @Override
    public List<StoredBook> findByUserId(Integer userId) {
        return delegate.findByUserId(userId);
    }

    @Override
    public List<StoredBook> findPublicBooks() {
        return delegate.findPublicBooks();
    }

    @Override
    public long getTotalSizeByUserId(Integer userId) {
        return delegate.getTotalSizeByUserId(userId);
    }

    @Override
    public byte[] getBookContent(Integer bookId) {
        // Контент файлов (PDF/EPUB) обычно слишком велик для Caffeine
        return delegate.getBookContent(bookId);
    }

    @Override
    public List<StoredBook> findByType(StoredBook.BookType type) {
        return delegate.findByType(type);
    }

    @Override
    public List<StoredBook> findOwnedBooksByUserId(Integer userId) {
        return delegate.findOwnedBooksByUserId(userId);
    }

    @Override
    public List<StoredBook> findPublicBooks(String query, String genre, String language, String sort, int offset, int limit) {
        return delegate.findPublicBooks(query, genre, language, sort, offset, limit);
    }

    @Override
    public long countPublicBooks(String query, String genre, String language) {
        return delegate.countPublicBooks(query, genre, language);
    }

    @Override
    public List<String> findAllGenres() {
        return delegate.findAllGenres();
    }

    @Override
    public List<String> findAllLanguages() {
        return delegate.findAllLanguages();
    }

    @Override
    public long getTotalStorageSize() {
        return delegate.getTotalStorageSize();
    }

    @Override
    public long getTotalBookCount() {
        return delegate.getTotalBookCount();
    }
}
