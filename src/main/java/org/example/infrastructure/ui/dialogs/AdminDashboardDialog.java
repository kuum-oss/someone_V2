package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.Notification;
import org.example.core.service.AdminDashboardService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardDialog extends JDialog {
    private final AdminDashboardService dashboardService;
    private final ResourceBundle messages;

    public AdminDashboardDialog(Frame parent, AdminDashboardService dashboardService, ResourceBundle messages) {
        super(parent, messages.getString("admin.dashboard.title"), true);
        this.dashboardService = dashboardService;
        this.messages = messages;
        initUI();
    }

    private void initUI() {
        setSize(800, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        statsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("admin.stats.title")));
        
        long count = dashboardService.getTotalBookCount();
        double size = dashboardService.getTotalDataVolumeGB();

        JLabel countLabel = new JLabel(MessageFormat.format(messages.getString("admin.stats.count"), count), SwingConstants.CENTER);
        countLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel sizeLabel = new JLabel(MessageFormat.format(messages.getString("admin.stats.size"), String.format("%.2f", size)), SwingConstants.CENTER);
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        statsPanel.add(countLabel);
        statsPanel.add(sizeLabel);
        add(statsPanel, BorderLayout.NORTH);

        // Notifications Panel
        JPanel notifyPanel = new JPanel(new BorderLayout());
        notifyPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("admin.notify.title")));

        String[] columns = {
                messages.getString("admin.notify.date"),
                messages.getString("admin.notify.msg"),
                messages.getString("admin.notify.status")
        };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        
        List<Notification> notifications = dashboardService.getNotifications();
        for (Notification n : notifications) {
            tableModel.addRow(new Object[]{
                n.getCreatedAt().toString(),
                n.getMessage(),
                n.isRead() ? messages.getString("admin.notify.read") : messages.getString("admin.notify.new")
            });
        }

        notifyPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        JButton markReadBtn = new JButton(messages.getString("admin.button.mark_read"));
        markReadBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Notification n = notifications.get(row);
                dashboardService.markNotificationAsRead(n.getId());
                tableModel.setValueAt(messages.getString("admin.notify.read"), row, 2);
            }
        });
        notifyPanel.add(markReadBtn, BorderLayout.SOUTH);

        add(notifyPanel, BorderLayout.CENTER);

        JButton closeBtn = new JButton(messages.getString("close"));
        closeBtn.addActionListener(e -> dispose());
        add(closeBtn, BorderLayout.SOUTH);
    }
}
