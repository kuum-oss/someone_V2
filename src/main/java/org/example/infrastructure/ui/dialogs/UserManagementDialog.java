package org.example.infrastructure.ui.dialogs;
import org.example.infrastructure.ui.BookLibraryGui;
import org.example.core.entity.User;
import org.example.core.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagementDialog extends JDialog {
    private final AdminService adminService;
    private final Frame parentFrame;
    private final ResourceBundle messages;

    public UserManagementDialog(Frame parent, AdminService adminService, ResourceBundle messages) {
        super(parent, messages.getString("admin.users.title"), true);
        this.parentFrame = parent;
        this.adminService = adminService;
        this.messages = messages;
        initUI();
    }

    private void initUI() {
        setSize(800, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        String[] columns = {
                messages.getString("admin.users.id"),
                messages.getString("admin.users.email"),
                messages.getString("admin.users.storage"),
                messages.getString("admin.users.is_admin")
        };
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);

        List<User> users = adminService.getAllUsers();
        for (User u : users) {
            long usage = adminService.getUserStorageUsage(u.getId());
            tableModel.addRow(new Object[]{
                u.getId(),
                u.getEmail(),
                String.format("%.2f", (double) usage / (1024 * 1024)),
                u.isAdmin() ? messages.getString("admin.users.yes") : messages.getString("admin.users.no")
            });
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        JButton banBtn = new JButton(messages.getString("admin.users.ban"));
        banBtn.setBackground(new Color(255, 100, 100));
        banBtn.setForeground(Color.WHITE);

        banBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Integer userId = (Integer) tableModel.getValueAt(row, 0);
                String email = (String) tableModel.getValueAt(row, 1);
                
                String reason = JOptionPane.showInputDialog(this, MessageFormat.format(messages.getString("admin.users.ban.reason"), email));
                if (reason != null && !reason.isBlank()) {
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        messages.getString("admin.users.ban.confirm"), 
                        messages.getString("dialog.confirm.title"), JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        adminService.handleUserBanAndCleanup(userId, reason);
                        tableModel.removeRow(row);
                        
                        if (parentFrame instanceof BookLibraryGui gui) {
                            SwingUtilities.invokeLater(() -> {
                                gui.refreshAuthState(); 
                            });
                        }
                        
                        JOptionPane.showMessageDialog(this, messages.getString("admin.users.ban.success"));
                        
                        if (parentFrame instanceof BookLibraryGui gui && !gui.isUserAuthenticated()) {
                            dispose(); 
                        }
                    }
                }
            }
        });

        actionPanel.add(banBtn);
        add(actionPanel, BorderLayout.SOUTH);
    }
}
