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
        String[] columns = {"Дата", "Книга", "Статус"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (Order o : orders) {
            model.addRow(new Object[]{o.getCreatedAt(), o.getBookTitle(), o.getStatus()});
        }
        JTable table = new JTable(model);
        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Мої замовлення", JOptionPane.PLAIN_MESSAGE);
    }
}
