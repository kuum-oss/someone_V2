package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.Book;
import org.example.core.entity.StoredBook;
import org.example.core.service.AdminService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

public class AddBookDialog extends JDialog {
    private final AdminService adminService;
    private final boolean isPhysical;
    private final ResourceBundle messages;
    
    private JTextField titleField;
    private JTextField authorField;
    private JTextField genreField;
    private JTextField priceField;
    private JLabel coverLabel;
    private byte[] coverData;
    private File bookFile;

    public AddBookDialog(Frame parent, AdminService adminService, boolean isPhysical, ResourceBundle messages) {
        super(parent, isPhysical ? messages.getString("dialog.add_book.physical") : messages.getString("dialog.add_book.ebook"), true);
        this.adminService = adminService;
        this.isPhysical = isPhysical;
        this.messages = messages;
        initUI();
    }

    private void initUI() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formPanel.add(new JLabel(messages.getString("dialog.add_book.title")));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel(messages.getString("dialog.add_book.author")));
        authorField = new JTextField();
        formPanel.add(authorField);

        formPanel.add(new JLabel(messages.getString("dialog.add_book.genre")));
        genreField = new JTextField();
        formPanel.add(genreField);

        formPanel.add(new JLabel(messages.getString("dialog.add_book.price")));
        priceField = new JTextField("0");
        formPanel.add(priceField);

        add(formPanel, BorderLayout.NORTH);

        // Drop Zone / Cover Selection
        JPanel dropZone = new JPanel(new BorderLayout());
        dropZone.setBorder(BorderFactory.createTitledBorder(isPhysical ? messages.getString("dialog.add_book.cover_physical") : messages.getString("dialog.add_book.cover_ebook")));
        
        coverLabel = new JLabel(messages.getString("dialog.add_book.drop_zone"), SwingConstants.CENTER);
        coverLabel.setPreferredSize(new Dimension(300, 200));
        dropZone.add(coverLabel, BorderLayout.CENTER);

        setupDragAndDrop(coverLabel);
        
        add(dropZone, BorderLayout.CENTER);

        JButton saveBtn = new JButton(messages.getString("dialog.add_book.save"));
        saveBtn.addActionListener(e -> saveBook());
        add(saveBtn, BorderLayout.SOUTH);
    }

    private void setupDragAndDrop(JLabel label) {
        new DropTarget(label, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    processFileList(files);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void processFileList(List<File> files) throws IOException {
        for (File f : files) {
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null) {
                    processFileList(List.of(children));
                }
            } else {
                processFile(f);
            }
        }
    }

    private void processFile(File f) throws IOException {
        String name = f.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            coverData = Files.readAllBytes(f.toPath());
            ImageIcon icon = new ImageIcon(coverData);
            Image img = icon.getImage().getScaledInstance(150, 200, Image.SCALE_SMOOTH);
            coverLabel.setIcon(new ImageIcon(img));
            coverLabel.setText("");
        } else if (!isPhysical && org.example.core.util.BookFileUtils.isBookFile(f.toPath())) {
            bookFile = f;
            if (titleField.getText().isEmpty()) {
                titleField.setText(f.getName());
            }
        } else if (!isPhysical) {
            // Игнорируем файлы, которые не являются книгами или обложками в режиме электронной книги
        } else {
            JOptionPane.showMessageDialog(this, messages.getString("dialog.add_book.error.jpeg_only"), messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBook() {
        if (titleField.getText().isEmpty() || (isPhysical && coverData == null) || (!isPhysical && bookFile == null)) {
            JOptionPane.showMessageDialog(this, messages.getString("dialog.add_book.error.fill_fields"), messages.getString("error.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int price = 0;
            try {
                price = Integer.parseInt(priceField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, messages.getString("dialog.add_book.error.invalid_price"), messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book.Builder builder = Book.builder()
                    .title(titleField.getText())
                    .author(authorField.getText())
                    .genre(genreField.getText())
                    .cover(coverData)
                    .price(price);

            if (!isPhysical) {
                builder.filePath(bookFile.toPath());
                builder.format(bookFile.getName().substring(bookFile.getName().lastIndexOf('.') + 1));
                adminService.uploadToShop(builder.build());
            } else {
                builder.format("PHYSICAL");
                // Simplified way for physical books - we don't have a direct method for them in adminService yet
                // but uploadToShop calls storageService.uploadBook(userId, book, true)
                // and we updated storageService.uploadBook(userId, book, true) to handle PHYSICAL
                adminService.uploadToShop(builder.build());
            }
            
            JOptionPane.showMessageDialog(this, messages.getString("dialog.add_book.success"));
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(messages.getString("dialog.add_book.error.save"), ex.getMessage()), messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
