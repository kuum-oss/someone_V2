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
        titleLabel.setPreferredSize(new Dimension(120, 40));

        add(coverLabel, BorderLayout.CENTER);
        add(titleLabel, BorderLayout.SOUTH);

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
}
