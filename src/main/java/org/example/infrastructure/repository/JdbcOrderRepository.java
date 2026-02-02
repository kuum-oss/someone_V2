package org.example.infrastructure.repository;

import org.example.core.entity.Order;
import org.example.core.repository.OrderRepository;
import org.example.infrastructure.db.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderRepository implements OrderRepository {

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (user_id, book_id, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, order.getUserId());
            ps.setInt(2, order.getBookId());
            ps.setString(3, order.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Order(rs.getInt(1), order.getUserId(), order.getBookId(), order.getStatus(), LocalDateTime.now());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving order", e);
        }
        return order;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.email, b.title FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "JOIN books b ON o.book_id = b.id";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("book_id"),
                        Order.Status.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                order.setUserEmail(rs.getString("email"));
                order.setBookTitle(rs.getString("title"));
                orders.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders", e);
        }
        return orders;
    }

    @Override
    public List<Order> findByUserId(Integer userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, b.title FROM orders o " +
                     "JOIN books b ON o.book_id = b.id " +
                     "WHERE o.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("book_id"),
                            Order.Status.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    order.setBookTitle(rs.getString("title"));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding orders by userId", e);
        }
        return orders;
    }

    @Override
    public void updateStatus(Integer orderId, Order.Status status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating order status", e);
        }
    }
}
