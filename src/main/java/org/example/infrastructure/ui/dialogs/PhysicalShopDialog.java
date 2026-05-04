package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.Order;
import org.example.core.entity.StoredBook;
import org.example.core.service.AuthService;
import org.example.core.service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PhysicalShopDialog extends JDialog {
    private final OrderService orderService;
    private final AuthService authService;

    public PhysicalShopDialog(Frame parent, OrderService orderService, AuthService authService) {
        super(parent, "Магазин фізичних книг", true);
        this.orderService = orderService;
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel bookPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        bookPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<StoredBook> physicalBooks = orderService.getPhysicalBooksForSale();
        for (StoredBook b : physicalBooks) {
            bookPanel.add(createBookCard(b));
        }

        add(new JScrollPane(bookPanel), BorderLayout.CENTER);

        JButton viewOrdersBtn = new JButton("Мої замовлення");
        viewOrdersBtn.addActionListener(e -> showUserOrders());
        add(viewOrdersBtn, BorderLayout.SOUTH);
    }

    private JPanel createBookCard(StoredBook book) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JLabel coverLabel = new JLabel();
        if (book.getCover() != null) {
            ImageIcon icon = new ImageIcon(book.getCover());
            Image img = icon.getImage().getScaledInstance(120, 160, Image.SCALE_SMOOTH);
            coverLabel.setIcon(new ImageIcon(img));
        } else {
            coverLabel.setText("Немає обкладинки");
        }
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(coverLabel, BorderLayout.CENTER);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.add(new JLabel(book.getTitle(), SwingConstants.CENTER));
        info.add(new JLabel(book.getAuthor(), SwingConstants.CENTER));
        
        JButton orderBtn = new JButton("Замовити");
        orderBtn.addActionListener(e -> {
            try {
                // Проверяем наличие активных заказов перед оформлением
                List<Order> userOrders = orderService.getUserOrders(authService.getCurrentUser().getId());
                boolean hasActive = userOrders.stream()
                        .anyMatch(o -> o.getBookId().equals(book.getId()) && 
                                      (o.getStatus() == Order.Status.PENDING || o.getStatus() == Order.Status.SHIPPED));
                
                if (hasActive) {
                    JOptionPane.showMessageDialog(this, "У вас вже є активне замовлення на цю книгу.");
                    return;
                }

                orderService.placeOrder(authService.getCurrentUser().getId(), book.getId());
                JOptionPane.showMessageDialog(this, "Замовлення оформлено! Адмін зв'яжеться з вами.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage());
            }
        });
        info.add(orderBtn);
        
        card.add(info, BorderLayout.SOUTH);
        return card;
    }

    private void showUserOrders() {
        List<Order> orders = orderService.getUserOrders(authService.getCurrentUser().getId());
        String[] columns = {"ID", "Дата", "Книга", "Статус", "Дія"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        for (Order o : orders) {
            model.addRow(new Object[]{o.getId(), o.getCreatedAt(), o.getBookTitle(), o.getStatus(), o.getStatus() == Order.Status.PENDING ? "Скасувати" : ""});
        }

        JTable table = new JTable(model);
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), (orderId) -> {
            try {
                orderService.cancelOrder((Integer) orderId, authService.getCurrentUser().getId());
                JOptionPane.showMessageDialog(this, "Замовлення скасовано");
                showUserOrders(); // Refresh
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Помилка: " + ex.getMessage());
            }
        }));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(this, scroll, "Мої замовлення", JOptionPane.PLAIN_MESSAGE);
    }

    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null || value.toString().isEmpty()) ? "" : value.toString());
            setVisible(!getText().isEmpty());
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private Object value;
        private final java.util.function.Consumer<Object> action;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, java.util.function.Consumer<Object> action) {
            super(checkBox);
            this.action = action;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.value = table.getModel().getValueAt(row, 0); // Get ID
            String text = (value == null) ? "" : value.toString();
            button.setText(text);
            button.setVisible(!text.isEmpty());
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (button.getText() != null && !button.getText().isEmpty()) {
                action.accept(value);
            }
            return button.getText();
        }
    }
}
