package org.example.core.service;

import org.example.core.entity.Notification;
import org.example.core.repository.BookRepository;
import org.example.core.repository.NotificationRepository;

import java.util.List;

public class AdminDashboardService {
    private final BookRepository bookRepository;
    private final NotificationRepository notificationRepository;

    public AdminDashboardService(BookRepository bookRepository, NotificationRepository notificationRepository) {
        this.bookRepository = bookRepository;
        this.notificationRepository = notificationRepository;
    }

    public long getTotalBookCount() {
        return bookRepository.getTotalBookCount();
    }

    public double getTotalDataVolumeGB() {
        long totalBytes = bookRepository.getTotalStorageSize();
        return (double) totalBytes / (1024 * 1024 * 1024);
    }

    public List<Notification> getNotifications() {
        return notificationRepository.findAll();
    }
    
    public void markNotificationAsRead(Integer id) {
        notificationRepository.markAsRead(id);
    }

    public void addNotification(Integer userId, String message) {
        notificationRepository.save(new Notification(null, userId, message, null, false));
    }
}
