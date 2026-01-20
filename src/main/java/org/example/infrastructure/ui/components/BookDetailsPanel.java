package org.example.infrastructure.ui.components;

import org.example.core.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class BookDetailsPanel extends JPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookDetailsPanel.class);
    private final JLabel coverLabel;
    private final JLabel authorPhotoLabel;
    private final JTextArea infoArea;
    private final JButton copyButton;
    private final JButton descriptionButton;
    private final JButton youtubeButton;
    private final JButton buyButton;
    private final JButton previewButton;
    private ResourceBundle messages;
    private Book currentBook;

    public BookDetailsPanel(ResourceBundle messages) {
        this.messages = messages;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 0));
        updateBorder();

        coverLabel = new JLabel();
        coverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        coverLabel.setPreferredSize(new Dimension(280, 380));

        authorPhotoLabel = new JLabel();
        authorPhotoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authorPhotoLabel.setPreferredSize(new Dimension(280, 200));
        authorPhotoLabel.setBorder(BorderFactory.createTitledBorder(messages.getString("details.author_photo")));

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(new Color(0, 0, 0, 0));
        infoArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        copyButton = new JButton(messages.getString("button.copy_info"));
        copyButton.addActionListener(e -> copyToClipboard());
        copyButton.setVisible(false);

        descriptionButton = new JButton(messages.getString("button.description"));
        descriptionButton.addActionListener(e -> showDescription());
        descriptionButton.setVisible(false);

        youtubeButton = new JButton(messages.getString("button.youtube"));
        youtubeButton.addActionListener(e -> watchReview());
        youtubeButton.setVisible(false);

        buyButton = new JButton(messages.getString("button.buy"));
        buyButton.setVisible(false);
        buyButton.setBackground(new Color(46, 139, 87));
        buyButton.setForeground(Color.WHITE);

        previewButton = new JButton(messages.getString("button.preview"));
        previewButton.setVisible(false);
        previewButton.setBackground(new Color(70, 130, 180));
        previewButton.setForeground(Color.WHITE);
        try {
            java.net.URL iconUrl = getClass().getResource("/icons/youtube.png");
            if (iconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                Image img = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                youtubeButton.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) {}

        JPanel photos = new JPanel();
        photos.setLayout(new BoxLayout(photos, BoxLayout.Y_AXIS));
        coverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        authorPhotoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        photos.add(coverLabel);
        photos.add(authorPhotoLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        buttonPanel.add(buyButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(descriptionButton);
        buttonPanel.add(youtubeButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(photos, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void updateDetails(Book book) {
        this.currentBook = book;
        updateImages(book);
        updateTextInfo(book);
        updateButtons(book);
    }

    private void updateImages(Book book) {
        setLabelIcon(coverLabel, book.getCover(), 280, 380, messages.getString("details.no_cover"));
        setLabelIcon(authorPhotoLabel, book.getAuthorPhoto(), 280, 200, null);
        authorPhotoLabel.setVisible(book.getAuthorPhoto() != null);
    }

    private void setLabelIcon(JLabel label, byte[] data, int maxW, int maxH, String fallbackText) {
        if (data != null && data.length > 0) {
            ImageIcon icon = new ImageIcon(data);
            if (icon.getIconWidth() > 0) {
                int imgW = icon.getIconWidth();
                int imgH = icon.getIconHeight();

                double scale = Math.min((double) maxW / imgW, (double) maxH / imgH);
                int targetW = (int) (imgW * scale);
                int targetH = (int) (imgH * scale);

                Image img = icon.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText(null);
                return;
            }
        }
        label.setIcon(null);
        label.setText(fallbackText);
    }

    private void updateTextInfo(Book book) {
        String info = String.format(
                "%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s\n%s: %s",
                messages.getString("details.title"), book.getTitle(),
                messages.getString("details.author"), book.getAuthor(),
                messages.getString("details.genre"), book.getGenre(),
                messages.getString("details.year"), book.getYear(),
                messages.getString("details.series"), book.getSeries(),
                messages.getString("details.language"), book.getLanguage(),
                messages.getString("details.path"), book.getFilePath()
        );
        infoArea.setText(info);
    }

    private void updateButtons(Book book) {
        copyButton.setVisible(book != null);
        descriptionButton.setVisible(book != null && book.getDescription() != null && !book.getDescription().isBlank());
        
        boolean hasTitleAndAuthor = book != null && book.getTitle() != null && !book.getTitle().equalsIgnoreCase("Unknown Title") &&
                book.getAuthor() != null && !book.getAuthor().equalsIgnoreCase("Unknown Author");
        youtubeButton.setVisible(hasTitleAndAuthor);
    }

    private void copyToClipboard() {
        if (currentBook == null) return;
        String text = infoArea.getText();
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(this, messages.getString("details.copy_info") + ": Success", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showDescription() {
        if (currentBook == null) return;
        String desc = currentBook.getDescription();
        if (desc == null || desc.isBlank()) {
            desc = messages.getString("dialog.description.none");
        }

        JTextArea textArea = new JTextArea(desc);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, 
                messages.getString("dialog.description.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void watchReview() {
        if (currentBook == null) return;
        try {
            String query = currentBook.getTitle() + " " + currentBook.getAuthor() + " Review";
            String url = "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    messages.getString("error.open_file") + ": " + e.getMessage(), 
                    messages.getString("error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setBuyAction(java.awt.event.ActionListener action) {
        for (java.awt.event.ActionListener al : buyButton.getActionListeners()) {
            buyButton.removeActionListener(al);
        }
        buyButton.addActionListener(action);
    }

    public void setBuyButtonVisible(boolean visible) {
        buyButton.setVisible(visible);
    }

    public void setPreviewButtonVisible(boolean visible) {
        previewButton.setVisible(visible);
    }

    public void setPreviewAction(java.awt.event.ActionListener action) {
        for (java.awt.event.ActionListener al : previewButton.getActionListeners()) {
            previewButton.removeActionListener(al);
        }
        previewButton.addActionListener(action);
    }

    public void setMessages(ResourceBundle messages) {
        this.messages = messages;
        updateBorder();
        authorPhotoLabel.setBorder(BorderFactory.createTitledBorder(messages.getString("details.author_photo")));
        copyButton.setText(messages.getString("button.copy_info"));
        descriptionButton.setText(messages.getString("button.description"));
        youtubeButton.setText(messages.getString("button.youtube"));
        buyButton.setText(messages.getString("button.buy"));
        previewButton.setText(messages.getString("button.preview"));
        if (currentBook != null) {
            updateDetails(currentBook);
        }
    }

    private void updateBorder() {
        setBorder(BorderFactory.createTitledBorder(messages.getString("panel.details")));
    }
}
