package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.LibrarySettings;
import org.example.core.service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminSettingsDialog extends JDialog {
    private final LibraryService libraryService;
    private final LibrarySettings settings;

    private JSpinner seatsSpinner;
    private JSpinner durationSpinner;
    private JTextField periodsField;

    public AdminSettingsDialog(Frame owner, LibraryService libraryService) {
        super(owner, "Налаштування бібліотеки", true);
        this.libraryService = libraryService;
        this.settings = libraryService.getSettings();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        
        form.add(new JLabel("Кількість місць:"));
        seatsSpinner = new JSpinner(new SpinnerNumberModel(settings.getTotalSeats(), 1, 500, 1));
        form.add(seatsSpinner);
        
        form.add(new JLabel("Період за замовчуванням (год):"));
        durationSpinner = new JSpinner(new SpinnerNumberModel(settings.getDefaultDurationHours(), 1, 24, 1));
        form.add(durationSpinner);
        
        form.add(new JLabel("Доступні періоди (через кому):"));
        periodsField = new JTextField(settings.getAvailablePeriods());
        form.add(periodsField);
        
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Скасувати");
        cancelBtn.addActionListener(e -> dispose());
        
        JButton saveBtn = new JButton("Зберегти");
        saveBtn.addActionListener(e -> {
            settings.setTotalSeats((Integer) seatsSpinner.getValue());
            settings.setDefaultDurationHours((Integer) durationSpinner.getValue());
            settings.setAvailablePeriods(periodsField.getText());
            libraryService.updateSettings(settings);
            JOptionPane.showMessageDialog(this, "Налаштування збережено!");
            dispose();
        });
        
        buttons.add(cancelBtn);
        buttons.add(saveBtn);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }
}
