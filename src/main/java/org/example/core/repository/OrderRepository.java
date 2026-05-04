package org.example.core.repository;

import org.example.core.entity.Order;
import java.util.List;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findAll();
    List<Order> findByUserId(Integer userId);
    List<Order> findByUserIdAndBookId(Integer userId, Integer bookId);
    void updateStatus(Integer orderId, Order.Status status);
}
