package org.example.infrastructure.ui.dialogs;

import org.example.core.util.BookFileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/** Пошаговый выбор и подтверждение импорта книг. */
public class ImportWizardDialog extends JDialog {
    private final ResourceBundle messages;
    private final Consumer<List<File>> onImport;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final DefaultListModel<String> selectedModel = new DefaultListModel<>();
    private final JLabel summaryLabel = new JLabel();
    private final JButton backButton = new JButton();
    private final JButton nextButton = new JButton();
    private final List<File> selectedFiles = new ArrayList<>();
    private int page;

    public ImportWizardDialog(Window owner, ResourceBundle messages, File initialDirectory,
                              Consumer<List<File>> onImport) {
        super(owner, messages.getString("import.title"), ModalityType.APPLICATION_MODAL);
        this.messages = messages;
        this.onImport = onImport;
        setLayout(new BorderLayout(10, 10));
        setMinimumSize(new Dimension(620, 420));
        setSize(680, 460);
        setLocationRelativeTo(owner);

        content.add(selectionPage(initialDirectory), "0");
        content.add(previewPage(), "1");
        add(content, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton.setText(messages.getString("import.back"));
        nextButton.setText(messages.getString("import.next"));
        backButton.setEnabled(false);
        backButton.addActionListener(e -> move(-1));
        nextButton.addActionListener(e -> move(1));
        actions.add(backButton);
        actions.add(nextButton);
        add(actions, BorderLayout.SOUTH);
    }

    private JPanel selectionPage(File initialDirectory) {
        JPanel page = pagePanel(messages.getString("import.step.select"));
        JButton chooseButton = new JButton(messages.getString("import.choose"));
        chooseButton.addActionListener(e -> chooseFiles(initialDirectory));
        page.add(chooseButton, BorderLayout.NORTH);
        page.add(new JScrollPane(new JList<>(selectedModel)), BorderLayout.CENTER);
        page.add(summaryLabel, BorderLayout.SOUTH);
        updateSummary();
        return page;
    }

    private JPanel previewPage() {
        JPanel page = pagePanel(messages.getString("import.step.preview"));
        JTextArea info = new JTextArea(messages.getString("import.preview.text"));
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setOpaque(false);
        page.add(info, BorderLayout.CENTER);
        return page;
    }

    private JPanel pagePanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(16, 16, 8, 16),
                BorderFactory.createTitledBorder(title)));
        return panel;
    }

    private void chooseFiles(File initialDirectory) {
        JFileChooser chooser = new JFileChooser(initialDirectory);
        chooser.setDialogTitle(messages.getString("chooser.add_books.title"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                messages.getString("chooser.add_books.filter"),
                "epub", "fb2", "pdf", "mobi", "djvu", "txt", "azw3"));
        chooser.setAcceptAllFileFilterUsed(true);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        selectedFiles.clear();
        File[] files = chooser.getSelectedFiles();
        if (files != null) selectedFiles.addAll(Arrays.asList(files));
        selectedModel.clear();
        selectedFiles.forEach(file -> selectedModel.addElement(file.getAbsolutePath()));
        updateSummary();
        nextButton.setEnabled(!selectedFiles.isEmpty());
    }

    private void updateSummary() {
        int count = selectedFiles.stream().mapToInt(this::estimateFileCount).sum();
        summaryLabel.setText(messages.getString("import.selected") + ": " + count);
    }

    private int estimateFileCount(File file) {
        if (file.isFile()) return BookFileUtils.isBookFile(file.toPath()) ? 1 : 0;
        File[] children = file.listFiles();
        if (children == null) return 0;
        int count = 0;
        for (File child : children) count += estimateFileCount(child);
        return count;
    }

    private void move(int direction) {
        page += direction;
        if (page == 1 && selectedFiles.isEmpty()) {
            page = 0;
            JOptionPane.showMessageDialog(this, messages.getString("import.empty"),
                    messages.getString("error.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (page > 1) {
            onImport.accept(new ArrayList<>(selectedFiles));
            dispose();
            return;
        }
        cards.show(content, String.valueOf(page));
        backButton.setEnabled(page > 0);
        nextButton.setText(page == 0 ? messages.getString("import.next") : messages.getString("import.start"));
    }
}
