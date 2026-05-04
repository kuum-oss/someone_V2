package org.example.core.service;

import org.example.core.entity.LibrarySettings;
import org.example.core.entity.Order;
import org.example.core.repository.LibrarySettingsRepository;
import org.example.core.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LibraryService {
    private final LibrarySettingsRepository settingsRepository;
    private final OrderRepository orderRepository;

    public LibraryService(LibrarySettingsRepository settingsRepository, OrderRepository orderRepository) {
        this.settingsRepository = settingsRepository;
        this.orderRepository = orderRepository;
    }

    public LibrarySettings getSettings() {
        return settingsRepository.getSettings();
    }

    public void updateSettings(LibrarySettings settings) {
        settingsRepository.updateSettings(settings);
    }

    public Set<String> getOccupiedSeats(LocalDateTime start, LocalDateTime end) {
        List<Order> allOrders = orderRepository.findAll();
        return allOrders.stream()
                .filter(o -> o.getStatus() != Order.Status.CANCELLED)
                .filter(o -> o.getSeatNumber() != null && o.getStartTime() != null && o.getEndTime() != null)
                .filter(o -> isOverlapping(start, end, o.getStartTime(), o.getEndTime()))
                .map(Order::getSeatNumber)
                .collect(Collectors.toSet());
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
