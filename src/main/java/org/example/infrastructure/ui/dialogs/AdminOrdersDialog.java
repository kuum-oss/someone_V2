package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.Order;
import org.example.core.service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class AdminOrdersDialog extends JDialog {
    private final OrderService orderService;
    private JTable table;
    private DefaultTableModel model;

    public AdminOrdersDialog(Frame owner, OrderService orderService) {
        super(owner, "Управление заказами", true);
        this.orderService = orderService;
        initUI();
    }

    private void initUI() {
        setSize(800, 500);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        String[] columns = {"ID", "Пользователь", "Книга", "Статус", "Действие"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        table = new JTable(model);
        refreshData();

        table.getColumnModel().getColumn(4).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionPanelEditor(new ActionHandler()));

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton scanQrBtn = new JButton("📷 Сканувати QR / Ввести номер");
        scanQrBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Введіть вміст QR-коду або номер замовлення:", "Перевірка замовлення", JOptionPane.QUESTION_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                input = input.trim();
                Order order = null;
                if (input.startsWith("ORDER:")) {
                    String[] parts = input.split(":");
                    if (parts.length >= 3) {
                        order = orderService.findOrderByQrToken(parts[2]);
                    }
                } else {
                    try {
                        int id = Integer.parseInt(input);
                        order = orderService.findOrderById(id);
                    } catch (NumberFormatException ignored) {}
                }

                if (order != null) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Замовлення #" + order.getId() + "\n" +
                                    "Користувач: " + order.getUserEmail() + "\n" +
                                    "Книга: " + order.getBookTitle() + "\n" +
                                    "Місце: " + (order.getSeatNumber() != null ? order.getSeatNumber() : "-") + "\n" +
                                    "Статус: " + order.getStatus() + "\n\n" +
                                    "Видати книгу (змінити статус на DELIVERED)?",
                            "Знайдено замовлення",
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        orderService.updateOrderStatus(order.getId(), Order.Status.DELIVERED);
                        refreshData();
                        JOptionPane.showMessageDialog(this, "Статус замовлення змінено на DELIVERED");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Замовлення не знайдено или невірний QR-код", "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton closeBtn = new JButton("Закрити");
        closeBtn.addActionListener(e -> dispose());
        JPanel bp = new JPanel();
        bp.add(scanQrBtn);
        bp.add(closeBtn);
        add(bp, BorderLayout.SOUTH);
    }

    private void refreshData() {
        model.setRowCount(0);
        List<Order> orders = orderService.getAllOrders();
        for (Order o : orders) {
            model.addRow(new Object[]{o.getId(), o.getUserEmail(), o.getBookTitle(), o.getStatus(), o});
        }
    }

    private class ActionHandler {
        public void execute(Order order) {
            orderService.updateOrderStatus(order.getId(), Order.Status.DELIVERED);
            refreshData();
        }
        public void cancel(Order order) {
            orderService.updateOrderStatus(order.getId(), Order.Status.CANCELLED);
            refreshData();
        }
    }

    static class ActionPanel extends JPanel {
        JButton execBtn = new JButton("Выполнить");
        JButton cancelBtn = new JButton("Отменить");

        public ActionPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            add(execBtn);
            add(cancelBtn);
        }
    }

    class ActionPanelRenderer extends ActionPanel implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Order o = (Order) value;
            setVisible(o.getStatus() == Order.Status.PENDING);
            return this;
        }
    }

    class ActionPanelEditor extends DefaultCellEditor {
        private final ActionPanel panel;
        private Order currentOrder;

        public ActionPanelEditor(ActionHandler handler) {
            super(new JCheckBox());
            panel = new ActionPanel();
            panel.execBtn.addActionListener(e -> {
                handler.execute(currentOrder);
                fireEditingStopped();
            });
            panel.cancelBtn.addActionListener(e -> {
                handler.cancel(currentOrder);
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentOrder = (Order) value;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return currentOrder;
        }
    }
}
