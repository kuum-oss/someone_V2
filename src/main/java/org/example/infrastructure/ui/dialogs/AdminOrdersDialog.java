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

        JButton closeBtn = new JButton("Закрыть");
        closeBtn.addActionListener(e -> dispose());
        JPanel bp = new JPanel();
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
