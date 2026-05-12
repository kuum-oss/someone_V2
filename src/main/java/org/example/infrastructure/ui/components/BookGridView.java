package org.example.infrastructure.ui.components;

import org.example.core.entity.Book;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BookGridView extends JPanel {
    private final List<BookGridItem> items = new ArrayList<>();
    private final Consumer<Book> onBookSelected;
    private final Consumer<Book> onBookDoubleClicked;
    private Book selectedBook;

    public BookGridView(Consumer<Book> onBookSelected, Consumer<Book> onBookDoubleClicked) {
        this.onBookSelected = onBookSelected;
        this.onBookDoubleClicked = onBookDoubleClicked;
        setLayout(new WrapLayout(FlowLayout.LEFT, 15, 15));
    }

    public void updateBooks(List<Book> books) {
        removeAll();
        items.clear();
        for (Book book : books) {
            BookGridItem item = new BookGridItem(book, 
                b -> selectBook(b), 
                onBookDoubleClicked
            );
            items.add(item);
            add(item);
        }
        revalidate();
        repaint();
    }

    private void selectBook(Book book) {
        this.selectedBook = book;
        onBookSelected.accept(book);
        updateVisualSelection(book);
    }

    private void updateVisualSelection(Book selected) {
        for (BookGridItem item : items) {
            item.setSelected(item.getBook().equals(selected));
        }
    }

    public Book getSelectedBook() {
        return selectedBook;
    }
}
