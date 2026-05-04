package org.example.infrastructure.ui.dialogs;

import org.example.core.entity.LibrarySettings;
import org.example.core.service.LibraryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class SeatSelectionDialog extends JDialog {
    private final LibraryService libraryService;
    private String selectedSeat = null;
    private LocalDateTime selectedStartTime = null;
    private LocalDateTime selectedEndTime = null;
    private final LibrarySettings settings;
    private final Set<String> occupiedSeats;
    
    private JPanel seatsPanel;
    private JComboBox<Integer> hourCombo;
    private JComboBox<Integer> durationCombo;
    private JLabel timeInfoLabel;

    public SeatSelectionDialog(Frame owner, LibraryService libraryService) {
        super(owner, "Вибір місця та часу", true);
        this.libraryService = libraryService;
        this.settings = libraryService.getSettings();
        
        // По умолчанию на сегодня через час
        LocalDateTime now = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
        this.occupiedSeats = libraryService.getOccupiedSeats(now, now.plusHours(settings.getDefaultDurationHours()));
        
        initUI();
        updateTimeInfo();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Panel: Time Selection
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Час (сьогодні):"));
        
        Integer[] hours = new Integer[24];
        for(int i=0; i<24; i++) hours[i] = i;
        hourCombo = new JComboBox<>(hours);
        hourCombo.setSelectedItem(LocalDateTime.now().getHour() + 1);
        hourCombo.addActionListener(e -> refreshOccupiedSeats());
        timePanel.add(hourCombo);
        
        timePanel.add(new JLabel("Період (год):"));
        String[] periodsStr = settings.getAvailablePeriods().split(",");
        Integer[] periods = new Integer[periodsStr.length];
        for(int i=0; i<periodsStr.length; i++) periods[i] = Integer.parseInt(periodsStr[i].trim());
        durationCombo = new JComboBox<>(periods);
        durationCombo.setSelectedItem(settings.getDefaultDurationHours());
        durationCombo.addActionListener(e -> refreshOccupiedSeats());
        timePanel.add(durationCombo);
        
        add(timePanel, BorderLayout.NORTH);

        // Center Panel: Seats Grid
        seatsPanel = new JPanel();
        int cols = 5;
        int rows = (int) Math.ceil(settings.getTotalSeats() / (double)cols);
        seatsPanel.setLayout(new GridLayout(rows, cols, 10, 10));
        
        renderSeats();
        
        JScrollPane scrollPane = new JScrollPane(seatsPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel: Info and Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        timeInfoLabel = new JLabel(" ");
        bottomPanel.add(timeInfoLabel, BorderLayout.NORTH);
        
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Скасувати");
        cancelBtn.addActionListener(e -> dispose());
        
        JButton okBtn = new JButton("Підтвердити");
        okBtn.addActionListener(e -> {
            if (selectedSeat == null) {
                JOptionPane.showMessageDialog(this, "Будь ласка, виберіть місце");
                return;
            }
            System.out.println("[DEBUG] Dialog OK pressed. Selected seat: " + selectedSeat);
            dispose();
        });
        
        buttons.add(cancelBtn);
        buttons.add(okBtn);
        bottomPanel.add(buttons, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void renderSeats() {
        seatsPanel.removeAll();
        ButtonGroup group = new ButtonGroup();
        for (int i = 1; i <= settings.getTotalSeats(); i++) {
            String seatName = "M" + i;
            boolean isOccupied = occupiedSeats.contains(seatName);
            
            JToggleButton seatBtn = new JToggleButton(seatName);
            seatBtn.setPreferredSize(new Dimension(60, 60));
            if (isOccupied) {
                seatBtn.setEnabled(false);
                seatBtn.setBackground(Color.LIGHT_GRAY);
                seatBtn.setToolTipText("Зайнято");
            } else {
                seatBtn.setBackground(new Color(230, 255, 230));
                seatBtn.addActionListener(e -> {
                    selectedSeat = seatName;
                    System.out.println("[DEBUG] Seat selected: " + selectedSeat);
                    updateTimeInfo();
                });
                group.add(seatBtn);
            }
            seatsPanel.add(seatBtn);
        }
        seatsPanel.revalidate();
        seatsPanel.repaint();
    }

    private void refreshOccupiedSeats() {
        int hour = (Integer) hourCombo.getSelectedItem();
        int duration = (Integer) durationCombo.getSelectedItem();
        
        LocalDateTime start = LocalDateTime.now().withHour(hour).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(duration);
        
        selectedStartTime = start;
        selectedEndTime = end;
        
        occupiedSeats.clear();
        occupiedSeats.addAll(libraryService.getOccupiedSeats(start, end));
        
        selectedSeat = null;
        renderSeats();
        updateTimeInfo();
    }

    private void updateTimeInfo() {
        int hour = (Integer) hourCombo.getSelectedItem();
        int duration = (Integer) durationCombo.getSelectedItem();
        LocalDateTime start = LocalDateTime.now().withHour(hour).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(duration);
        
        selectedStartTime = start;
        selectedEndTime = end;
        
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        String text = String.format("Доступний час: сьогодні з %s до %s", start.format(df), end.format(df));
        if (selectedSeat != null) {
            text += " | Вибрано місце: " + selectedSeat;
        }
        timeInfoLabel.setText(text);
    }

    public String getSelectedSeat() { return selectedSeat; }
    public LocalDateTime getStartTime() { return selectedStartTime; }
    public LocalDateTime getEndTime() { return selectedEndTime; }
}
