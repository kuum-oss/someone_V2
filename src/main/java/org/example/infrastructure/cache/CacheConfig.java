package org.example.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.core.entity.StoredBook;

import java.util.concurrent.TimeUnit;

public class CacheConfig {

    // Кэш метаданных книг по ID.
    // maximumSize было 5_000_000 — это баг: столько объектов со всеми
    // полями (включая обложки) кладёт JVM на лопатки при большом каталоге.
    // 2000 записей — разумный размер "горячего" кэша для UI (последние открытые/просматриваемые книги).
    private static final Cache<Integer, StoredBook> bookCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(2_000)
            .recordStats() // можно снять метрики через bookCache.stats() для мониторинга hit-rate
            .build();

    // Отдельный кэш обложек — по весу (в байтах), а не по количеству записей.
    // Обложки — самое тяжёлое, что тянет память вниз, поэтому у них своя политика вытеснения.
    private static final Cache<Integer, byte[]> coverCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumWeight(64L * 1024 * 1024) // ~64 MB суммарно на обложки
            .weigher((Integer key, byte[] cover) -> cover.length)
            .recordStats()
            .build();

    public static Cache<Integer, StoredBook> getBookCache() {
        return bookCache;
    }

    public static Cache<Integer, byte[]> getCoverCache() {
        return coverCache;
    }

    public static void invalidateBook(Integer bookId) {
        if (bookId != null) {
            bookCache.invalidate(bookId);
            coverCache.invalidate(bookId);
        }
    }

    public static void invalidateAll() {
        bookCache.invalidateAll();
        coverCache.invalidateAll();
    }
}
