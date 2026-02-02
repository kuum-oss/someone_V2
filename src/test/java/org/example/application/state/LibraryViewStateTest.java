package org.example.application.state;

import org.example.core.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibraryViewStateTest {

    private LibraryViewState state;

    @BeforeEach
    void setUp() {
        state = new LibraryViewState();
    }

    @Test
    void testStateSeparationBetweenModes() {
        Book localBook = Book.builder().title("Local Book").build();
        Book shopBook = Book.builder().title("Shop Book").build();
        Book physicalBook = Book.builder().title("Physical Book").build();

        // 1. В режиме LIBRARY устанавливаем книги
        state.setMode(ViewMode.LIBRARY);
        state.setBooks(Collections.singletonList(localBook));

        assertEquals(1, state.getBooks().size());
        assertEquals("Local Book", state.getBooks().get(0).getTitle());

        // 2. Переключаемся в режим SHOP и устанавливаем книги магазина
        state.setMode(ViewMode.SHOP);
        state.setBooks(Collections.singletonList(shopBook));

        assertEquals(1, state.getBooks().size());
        assertEquals("Shop Book", state.getBooks().get(0).getTitle());

        // 3. Переключаемся в PHYSICAL_SHOP
        state.setMode(ViewMode.PHYSICAL_SHOP);
        state.setBooks(Collections.singletonList(physicalBook));

        assertEquals(1, state.getBooks().size());
        assertEquals("Physical Book", state.getBooks().get(0).getTitle());

        // 4. Проверяем, что локальные книги сохранились
        assertEquals(1, state.getLocalBooks().size());
        assertEquals("Local Book", state.getLocalBooks().get(0).getTitle());

        // 5. Переключаемся обратно в LIBRARY
        state.setMode(ViewMode.LIBRARY);
        assertEquals(1, state.getBooks().size());
        assertEquals("Local Book", state.getBooks().get(0).getTitle());
        
        // Убеждаемся, что остальные списки не пострадали
        assertEquals(1, state.getShopBooks().size());
        assertEquals("Shop Book", state.getShopBooks().get(0).getTitle());
        assertEquals(1, state.getPhysicalShopBooks().size());
        assertEquals("Physical Book", state.getPhysicalShopBooks().get(0).getTitle());
    }
}
