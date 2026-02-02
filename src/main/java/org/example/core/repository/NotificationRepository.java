package org.example.core.repository;

import org.example.core.entity.Notification;
import java.util.List;

public interface NotificationRepository {
    void save(Notification notification);
    List<Notification> findAll();
    List<Notification> findByUserId(Integer userId);
    void markAsRead(Integer notificationId);
}
