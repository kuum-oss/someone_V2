package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.StoredBook;
import org.example.core.service.AdminService;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class EditBookDialog extends JDialog {
    private final AdminService adminService;
    private final StoredBook book;
    private final ResourceBundle messages;

    private JTextField titleField;
    private JTextField authorField;
    private JTextField genreField;
    private JTextField languageField;
    private JTextField yearField;
    private JTextField priceField;
    private JComboBox<StoredBook.BookType> bookTypeComboBox;
    private JTextArea descriptionArea;

    public EditBookDialog(Frame parent, StoredBook book, AdminService adminService, ResourceBundle messages) {
        super(parent, messages.getString("dialog.edit_book.title"), true);
        this.book = book;
        this.adminService = adminService;
        this.messages = messages;
        initUI();
    }

    private void initUI() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel(messages.getString("details.title")));
        titleField = new JTextField(book.getTitle());
        formPanel.add(titleField);

        formPanel.add(new JLabel(messages.getString("details.author")));
        authorField = new JTextField(book.getAuthor());
        formPanel.add(authorField);

        formPanel.add(new JLabel(messages.getString("details.genre")));
        genreField = new JTextField(book.getGenre());
        formPanel.add(genreField);

        formPanel.add(new JLabel(messages.getString("details.language")));
        languageField = new JTextField(book.getLanguage());
        formPanel.add(languageField);

        formPanel.add(new JLabel(messages.getString("details.year")));
        yearField = new JTextField(book.getYear());
        formPanel.add(yearField);

        formPanel.add(new JLabel(messages.getString("dialog.edit_book.type")));
        bookTypeComboBox = new JComboBox<>(StoredBook.BookType.values());
        bookTypeComboBox.setSelectedItem(book.getBookType());
        formPanel.add(bookTypeComboBox);

        formPanel.add(new JLabel(messages.getString("dialog.add_book.price")));
        priceField = new JTextField(String.valueOf(book.getPrice()));
        formPanel.add(priceField);

        add(formPanel, BorderLayout.NORTH);

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("details.description")));
        descriptionArea = new JTextArea(book.getDescription());
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        add(descPanel, BorderLayout.CENTER);

        JButton saveBtn = new JButton(messages.getString("dialog.add_book.save"));
        saveBtn.addActionListener(e -> saveBook());
        add(saveBtn, BorderLayout.SOUTH);
    }

    private void saveBook() {
        try {
            int price = Integer.parseInt(priceField.getText().trim());
            adminService.updateBook(
                    book.getId(),
                    titleField.getText(),
                    authorField.getText(),
                    genreField.getText(),
                    languageField.getText(),
                    yearField.getText(),
                    descriptionArea.getText(),
                    price,
                    (StoredBook.BookType) bookTypeComboBox.getSelectedItem()
            );
            JOptionPane.showMessageDialog(this, messages.getString("dialog.edit_book.message.success"));
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, messages.getString("dialog.edit_book.message.error") + ": " + e.getMessage());
        }
    }
}
