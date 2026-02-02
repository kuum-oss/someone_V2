package org.example.core.repository;

import org.example.core.entity.StoredBook;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    StoredBook save(StoredBook book);
    List<StoredBook> findByUserId(Integer userId);
    List<StoredBook> findPublicBooks();
    long getTotalSizeByUserId(Integer userId);
    byte[] getBookContent(Integer bookId);
    Optional<StoredBook> findById(Integer id);
    void deleteById(Integer id);
    List<StoredBook> findByType(StoredBook.BookType type);
    List<StoredBook> findOwnedBooksByUserId(Integer userId);
    long getTotalStorageSize();
    long getTotalBookCount();
}
