package org.example.infrastructure.ui.dialogs;

import org.example.application.controller.BookLibraryController;
import org.example.core.entity.BookReview;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class ReviewModerationDialog extends JDialog {
    private final BookLibraryController controller;
    private final ResourceBundle messages;
    private DefaultTableModel tableModel;
    private JTable table;

    public ReviewModerationDialog(Frame parent, BookLibraryController controller, ResourceBundle messages) {
        super(parent, messages.getString("admin.reviews.title"), true);
        this.controller = controller;
        this.messages = messages;
        initUI();
        loadReviews();
    }

    private void initUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        String[] columns = {
                "ID",
                messages.getString("admin.reviews.book"),
                messages.getString("admin.reviews.user"),
                messages.getString("admin.reviews.text")
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(450);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton deleteBtn = new JButton(messages.getString("button.delete_review"));
        deleteBtn.setBackground(new Color(255, 100, 100));
        deleteBtn.setForeground(Color.WHITE);

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int reviewId = (int) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this,
                        messages.getString("dialog.delete_review.confirm"),
                        messages.getString("dialog.delete_review.title"),
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    controller.deleteReview(reviewId);
                    tableModel.removeRow(row);
                }
            }
        });

        JButton refreshBtn = new JButton(messages.getString("button.refresh"));
        refreshBtn.addActionListener(e -> loadReviews());

        actionPanel.add(refreshBtn);
        actionPanel.add(deleteBtn);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadReviews() {
        tableModel.setRowCount(0);
        List<BookReview> reviews = controller.getAllReviews();
        for (BookReview review : reviews) {
            tableModel.addRow(new Object[]{
                    review.getId(),
                    review.getBookTitle(),
                    review.getReviewerName(),
                    review.getReviewText()
            });
        }
    }
}
