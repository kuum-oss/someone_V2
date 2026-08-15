package org.example.infrastructure.ui.components;

import org.example.core.entity.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class BookGridItem extends JPanel {
    private final Book book;
    private final Consumer<Book> onSelected;
    private final Consumer<Book> onDoubleClicked;
    private boolean selected;

    public BookGridItem(Book book, Consumer<Book> onSelected, Consumer<Book> onDoubleClicked) {
        this.book = book;
        this.onSelected = onSelected;
        this.onDoubleClicked = onDoubleClicked;
        initUI();
    }

    public Book getBook() {
        return book;
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(140, 200));
        setOpaque(false);

        // Обложка
        JLabel coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (book.getCover() != null && book.getCover().length > 0) {
            ImageIcon icon = new ImageIcon(book.getCover());
            Image img = icon.getImage().getScaledInstance(110, 150, Image.SCALE_SMOOTH);
            coverLabel.setIcon(new ImageIcon(img));
        } else {
            // Заглушка, если нет обложки
            coverLabel.setText("No Cover");
            coverLabel.setPreferredSize(new Dimension(110, 150));
            coverLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }

        // Название
        JLabel titleLabel = new JLabel("<html><center>" + book.getTitle() + "</center></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setVerticalAlignment(SwingConstants.TOP);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));
        titleLabel.setPreferredSize(new Dimension(120, 34));

        JLabel statusLabel = new JLabel(metadataStatus(book));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 10f));
        statusLabel.setForeground(metadataWarning(book) ? new Color(190, 120, 0)
                : new Color(45, 135, 75));

        JPanel footer = new JPanel(new BorderLayout(0, 2));
        footer.setOpaque(false);
        footer.add(titleLabel, BorderLayout.CENTER);
        footer.add(statusLabel, BorderLayout.SOUTH);

        add(coverLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onSelected.accept(book);
                if (e.getClickCount() == 2) {
                    onDoubleClicked.accept(book);
                }
            }
        });
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setOpaque(selected);
        setBackground(UIManager.getColor("List.selectionBackground"));
        repaint();
    }

    private String metadataStatus(Book book) {
        return metadataWarning(book) ? "⚠ Требует проверки" : "✓ Проверена";
    }

    private boolean metadataWarning(Book book) {
        return isUnknown(book.getTitle()) || isUnknown(book.getAuthor())
                || isUnknown(book.getGenre()) || isUnknown(book.getYear());
    }

    private boolean isUnknown(String value) {
        return value == null || value.isBlank() || "Unknown".equalsIgnoreCase(value)
                || "Неизвестно".equalsIgnoreCase(value);
    }
}
