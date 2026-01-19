package org.example.core.repository;

import org.example.core.entity.StoredBook;
import java.util.List;

public interface BookRepository {
    StoredBook save(StoredBook book);
    List<StoredBook> findByUserId(Integer userId);
    long getTotalSizeByUserId(Integer userId);
    byte[] getBookContent(Integer bookId);
}
