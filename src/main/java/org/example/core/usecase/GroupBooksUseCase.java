package org.example.core.usecase;

import org.example.core.entity.Book;
import java.util.List;
import java.util.Map;
// Use case для группировки книг по выбранному критерию (жанр, автор или год)

public interface GroupBooksUseCase {

    enum GroupMode {
        GENRE, AUTHOR, YEAR
    }

    Map<String, List<Book>> execute(List<Book> books, GroupMode mode);
}
