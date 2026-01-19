package org.example.core.usecase.impl;

import org.example.core.entity.Book;
import org.example.core.usecase.GroupBooksUseCase;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class GroupBooksUseCaseImpl implements GroupBooksUseCase {

    @Override
    public Map<String, List<Book>> execute(List<Book> books, GroupMode mode) {
        if (books == null) return new TreeMap<>();
        
        return books.stream().collect(
                Collectors.groupingBy(
                        book -> getGroupKey(book, mode),
                        TreeMap::new,
                        Collectors.toList()
                )
        );
    }

    private String getGroupKey(Book book, GroupMode mode) {
        return switch (mode) {
            case AUTHOR -> book.getAuthor();
            case YEAR -> book.getYear();
            default -> book.getGenre();
        };
    }
}
