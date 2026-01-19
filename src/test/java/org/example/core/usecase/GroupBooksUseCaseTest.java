package org.example.core.usecase;

import org.example.core.entity.Book;
import org.example.core.usecase.impl.GroupBooksUseCaseImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupBooksUseCaseTest {

    private final GroupBooksUseCase useCase = new GroupBooksUseCaseImpl();

    @Test
    void shouldGroupByGenre() {
        Book b1 = Book.builder().title("B1").genre("Sci-Fi").filePath(Paths.get("f1")).build();
        Book b2 = Book.builder().title("B2").genre("Fantasy").filePath(Paths.get("f2")).build();
        Book b3 = Book.builder().title("B3").genre("Sci-Fi").filePath(Paths.get("f3")).build();

        Map<String, List<Book>> result = useCase.execute(Arrays.asList(b1, b2, b3), GroupBooksUseCase.GroupMode.GENRE);

        assertEquals(2, result.size());
        assertEquals(2, result.get("Sci-Fi").size());
        assertEquals(1, result.get("Fantasy").size());
    }

    @Test
    void shouldGroupByAuthor() {
        Book b1 = Book.builder().title("B1").author("Author A").filePath(Paths.get("f1")).build();
        Book b2 = Book.builder().title("B2").author("Author B").filePath(Paths.get("f2")).build();
        Book b3 = Book.builder().title("B3").author("Author A").filePath(Paths.get("f3")).build();

        Map<String, List<Book>> result = useCase.execute(Arrays.asList(b1, b2, b3), GroupBooksUseCase.GroupMode.AUTHOR);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("Author A"));
        assertTrue(result.containsKey("Author B"));
        assertEquals(2, result.get("Author A").size());
    }
}
