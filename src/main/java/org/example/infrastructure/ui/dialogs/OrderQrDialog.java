package org.example.infrastructure.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.example.core.entity.Order;

/**
 * Dialog displayed after a successful physical book order,
 * showing the QR code ticket and order details.
 */
public class OrderQrDialog extends JDialog {

    public OrderQrDialog(Frame owner, Order order, byte[] qrPngBytes) {
        super(owner, "Замовлення підтверджено!", true);
        initUI(order, qrPngBytes);
    }

    private void initUI(Order order, byte[] qrPngBytes) {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("✅ Замовлення #" + order.getId() + " підтверджено!", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // Center: QR code
        JPanel center = new JPanel(new BorderLayout(5, 5));
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(qrPngBytes));
            Image scaled = img.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            JLabel qrLabel = new JLabel(new ImageIcon(scaled));
            qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            center.add(qrLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            center.add(new JLabel("QR-код недоступний"), BorderLayout.CENTER);
        }

        JPanel info = new JPanel(new GridLayout(0, 1, 4, 4));
        info.add(new JLabel("📚 Книга: " + order.getBookTitle()));
        info.add(new JLabel("💺 Місце: " + (order.getSeatNumber() != null ? order.getSeatNumber() : "-")));
        if (order.getStartTime() != null) {
            info.add(new JLabel("🕒 Час: " + order.getStartTime().toLocalTime() + " – " + order.getEndTime().toLocalTime()));
        }
        info.add(new JLabel("📧 QR-код надіслано на вашу пошту."));
        info.add(new JLabel("🔢 Або назвіть адміну номер: #" + order.getId()));
        center.add(info, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        // Buttons
        JButton saveBtn = new JButton("💾 Зберегти QR-код");
        saveBtn.addActionListener(e -> {
            if (qrPngBytes == null || qrPngBytes.length == 0) {
                JOptionPane.showMessageDialog(this, "QR-код недоступний", "Помилка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Зберегти QR-код");
            fileChooser.setSelectedFile(new java.io.File("order_" + order.getId() + "_qr.png"));
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(fileToSave)) {
                    fos.write(qrPngBytes);
                    JOptionPane.showMessageDialog(this, "QR-код успішно збережено:\n" + fileToSave.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Помилка збереження: " + ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton closeBtn = new JButton("Закрити");
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.add(saveBtn);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(320, 450));
        setLocationRelativeTo(getOwner());
    }
}
