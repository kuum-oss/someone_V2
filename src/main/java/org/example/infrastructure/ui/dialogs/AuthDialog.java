package org.example.infrastructure.ui.dialogs;

import org.example.core.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class AuthDialog extends JDialog {
    private final AuthService authService;
    private final ResourceBundle messages;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox adminCheckBox;
    private boolean succeeded;

    public AuthDialog(Frame parent, AuthService authService, ResourceBundle messages) {
        super(parent, messages.getString("menu.login"), true);
        this.authService = authService;
        this.messages = messages;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        fieldsPanel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        fieldsPanel.add(emailField);

        fieldsPanel.add(new JLabel(messages.getString("details.password") != null ? messages.getString("details.password") : "Password:"));
        passwordField = new JPasswordField(20);
        fieldsPanel.add(passwordField);

        fieldsPanel.add(new JLabel(messages.getString("admin.users.is_admin") + ":"));
        adminCheckBox = new JCheckBox();
        fieldsPanel.add(adminCheckBox);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        JButton loginButton = new JButton(messages.getString("menu.login"));
        JButton registerButton = new JButton(messages.getString("menu.register"));

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            try {
                if (authService.login(email, password)) {
                    succeeded = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, messages.getString("error.invalid_login"), messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
                }
            } catch (SecurityException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), messages.getString("error.access_denied"), JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, messages.getString("dialog.add_book.error.fill_fields"), messages.getString("error.title"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (authService.register(email, password, adminCheckBox.isSelected())) {
                JOptionPane.showMessageDialog(this, messages.getString("msg.register_success"));
            } else {
                JOptionPane.showMessageDialog(this, messages.getString("error.user_exists"), messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);
        add(buttonsPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
