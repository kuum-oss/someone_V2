package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.StoredBook;
import org.example.core.service.FileStorageService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class LibraryDialog extends JDialog {
    private final Integer userId;
    private final FileStorageService storageService;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JLabel quotaLabel;

    public LibraryDialog(Frame parent, Integer userId, FileStorageService storageService) {
        super(parent, "Моя библиотека", true);
        this.userId = userId;
        this.storageService = storageService;
        initUI();
        loadBooks();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(700, 450);

        tableModel = new DefaultTableModel(new Object[]{"ID", "Название", "Размер (КБ)", "Оригинальное имя"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        countLabel = new JLabel("Всего книг: 0");
        countLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 10));
        quotaLabel = new JLabel("Занято: 0 Б из 5 ГБ");
        quotaLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 10));
        infoPanel.add(countLabel);
        infoPanel.add(quotaLabel);
        
        bottomPanel.add(infoPanel, BorderLayout.WEST);

        JButton downloadButton = new JButton("Скачать");
        downloadButton.addActionListener(e -> downloadSelectedBook());
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(downloadButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        List<StoredBook> books = storageService.getUserBooks(userId);
        long totalSize = 0;
        for (StoredBook book : books) {
            tableModel.addRow(new Object[]{
                book.getId(), 
                book.getTitle(), 
                book.getFileSize() / 1024, 
                book.getOriginalName()
            });
            totalSize += book.getFileSize();
        }
        
        countLabel.setText("Всего книг: " + books.size());
        
        double usedGB = totalSize / (1024.0 * 1024.0 * 1024.0);
        if (usedGB < 0.1) {
            double usedMB = totalSize / (1024.0 * 1024.0);
            quotaLabel.setText(String.format("Занято: %.2f МБ из 5 ГБ", usedMB));
        } else {
            quotaLabel.setText(String.format("Занято: %.2f ГБ из 5 ГБ", usedGB));
        }
    }

    private void downloadSelectedBook() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите книгу для скачивания", "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Integer bookId = (Integer) tableModel.getValueAt(modelRow, 0);
        
        try {
            byte[] content = storageService.getBookContent(bookId);
            if (content != null) {
                String originalName = (String) tableModel.getValueAt(modelRow, 3);
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Сохранить книгу");
                fileChooser.setSelectedFile(new File(originalName));
                
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    Path target = fileChooser.getSelectedFile().toPath();
                    Files.write(target, content);
                    JOptionPane.showMessageDialog(this, "Файл успешно сохранен!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось получить содержимое файла из БД (ID: " + bookId + ")", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при скачивании или сохранении файла: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
