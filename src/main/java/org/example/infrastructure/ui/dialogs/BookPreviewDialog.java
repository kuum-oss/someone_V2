package org.example.infrastructure.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class BookPreviewDialog extends JDialog {
    public BookPreviewDialog(Frame parent, String title, String previewText, ResourceBundle messages) {
        super(parent, title, true);
        initUI(previewText, messages);
    }

    private void initUI(String previewText, ResourceBundle messages) {
        setSize(600, 800);
        setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea(previewText);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton(messages.getString("close"));
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(getParent());
    }
}
