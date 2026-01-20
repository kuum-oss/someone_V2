package org.example.infrastructure.ui.dialogs;

import org.example.core.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class AuthDialog extends JDialog {
    private final AuthService authService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox adminCheckBox;
    private boolean succeeded;

    public AuthDialog(Frame parent, AuthService authService) {
        super(parent, "Авторизация", true);
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        fieldsPanel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        fieldsPanel.add(emailField);

        fieldsPanel.add(new JLabel("Пароль:"));
        passwordField = new JPasswordField(20);
        fieldsPanel.add(passwordField);

        fieldsPanel.add(new JLabel("Администратор:"));
        adminCheckBox = new JCheckBox();
        fieldsPanel.add(adminCheckBox);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        JButton loginButton = new JButton("Войти");
        JButton registerButton = new JButton("Регистрация");

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            if (authService.login(email, password)) {
                if (adminCheckBox.isSelected()) {
                    authService.getCurrentUser().setAdmin(true);
                }
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Неверный email или пароль", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все поля", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (authService.register(email, password, adminCheckBox.isSelected())) {
                JOptionPane.showMessageDialog(this, "Регистрация успешна! Теперь вы можете войти.");
            } else {
                JOptionPane.showMessageDialog(this, "Пользователь с таким email уже существует", "Ошибка", JOptionPane.ERROR_MESSAGE);
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
