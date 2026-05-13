package org.example.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.core.entity.StoredBook;

import java.util.concurrent.TimeUnit;

public class CacheConfig {
    
    // Кэш для отдельных книг по ID (включая метаданные и обложки)
    private static final Cache<Integer, StoredBook> bookCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(500)
            .build();

    public static Cache<Integer, StoredBook> getBookCache() {
        return bookCache;
    }

    public static void invalidateBook(Integer bookId) {
        if (bookId != null) {
            bookCache.invalidate(bookId);
        }
    }
}
