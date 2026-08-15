package org.example.infrastructure.ui.components;

import org.example.core.entity.Book;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;

/** Краткая сводка коллекции над основным списком книг. */
public class LibrarySummaryPanel extends JPanel {
    private final JLabel totalValue = valueLabel();
    private final JLabel authorsValue = valueLabel();
    private final JLabel genresValue = valueLabel();
    private final JLabel warningsValue = valueLabel();

    public LibrarySummaryPanel(ResourceBundle messages) {
        setLayout(new GridLayout(1, 4, 8, 0));
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(card(messages.getString("summary.total"), totalValue));
        add(card(messages.getString("summary.authors"), authorsValue));
        add(card(messages.getString("summary.genres"), genresValue));
        add(card(messages.getString("summary.warnings"), warningsValue));
    }

    public void updateBooks(List<Book> books) {
        List<Book> safeBooks = books == null ? List.of() : books;
        Set<String> authors = new HashSet<>();
        Set<String> genres = new HashSet<>();
        int warnings = 0;
        for (Book book : safeBooks) {
            authors.add(book.getAuthor());
            genres.add(book.getGenre());
            if (isUnknown(book.getAuthor()) || isUnknown(book.getTitle())
                    || isUnknown(book.getGenre()) || isUnknown(book.getYear())) {
                warnings++;
            }
        }
        totalValue.setText(String.valueOf(safeBooks.size()));
        authorsValue.setText(String.valueOf(authors.size()));
        genresValue.setText(String.valueOf(genres.size()));
        warningsValue.setText(String.valueOf(warnings));
    }

    private boolean isUnknown(String value) {
        return value == null || value.isBlank() || "Unknown".equalsIgnoreCase(value)
                || "Неизвестно".equalsIgnoreCase(value);
    }

    private JPanel card(String title, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        return label;
    }
}
