package org.example.infrastructure.repository;

import org.example.core.entity.Notification;
import org.example.core.repository.NotificationRepository;
import org.example.infrastructure.db.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcNotificationRepository implements NotificationRepository {

    @Override
    public void save(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (notification.getUserId() != null) {
                ps.setInt(1, notification.getUserId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, notification.getMessage());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving notification", e);
        }
    }

    @Override
    public List<Notification> findAll() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                        rs.getString("message"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getBoolean("is_read")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding notifications", e);
        }
        return notifications;
    }

    @Override
    public List<Notification> findByUserId(Integer userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? OR user_id IS NULL ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                            rs.getInt("id"),
                            rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                            rs.getString("message"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getBoolean("is_read")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding notifications by userId", e);
        }
        return notifications;
    }

    @Override
    public void markAsRead(Integer notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking notification as read", e);
        }
    }
}
