package org.example.core.service;

import org.example.core.entity.Order;
import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.core.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final AdminDashboardService dashboardService;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository, AdminDashboardService dashboardService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.dashboardService = dashboardService;
    }

    public Order placeOrder(Integer userId, Integer bookId) {
        return placeOrder(userId, bookId, null, null, null);
    }

    public Order placeOrder(Integer userId, Integer bookId, String seatNumber, LocalDateTime startTime, LocalDateTime endTime) {
        System.out.println("[DEBUG] OrderService.placeOrder called for user " + userId + ", book " + bookId + ", seat " + seatNumber);
        StoredBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        if (book.getBookType() != StoredBook.BookType.PHYSICAL) {
            throw new IllegalArgumentException("Only physical books can be ordered");
        }

        // Проверяем, есть ли уже активный заказ на эту книгу у этого пользователя
        List<Order> existingOrders = orderRepository.findByUserIdAndBookId(userId, bookId);
        boolean hasActiveOrder = existingOrders.stream()
                .anyMatch(o -> o.getStatus() == Order.Status.PENDING || o.getStatus() == Order.Status.SHIPPED);
        
        if (hasActiveOrder) {
            throw new IllegalStateException("У вас уже есть активный заказ на эту книгу. Пожалуйста, дождитесь выполнения или отмените текущий заказ.");
        }
        
        Order order = new Order(null, userId, bookId, Order.Status.PENDING, LocalDateTime.now(), seatNumber, startTime, endTime);
        Order savedOrder = orderRepository.save(order);
        System.out.println("[DEBUG] Order saved with ID: " + savedOrder.getId());
        
        // Notify admin
        String msg = "New order for physical book: " + book.getTitle();
        if (seatNumber != null) {
            msg += " (Seat: " + seatNumber + ", Time: " + startTime + " to " + endTime + ")";
        }
        dashboardService.addNotification(null, msg);
        
        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getUserOrders(Integer userId) {
        return orderRepository.findByUserId(userId);
    }

    public void updateOrderStatus(Integer orderId, Order.Status status) {
        orderRepository.updateStatus(orderId, status);
        
        // Notify user about status change
        orderRepository.findAll().stream()
            .filter(o -> o.getId().equals(orderId))
            .findFirst()
            .ifPresent(order -> {
                String msg = "Статус вашего заказа на книгу \"" + order.getBookTitle() + "\" изменен на: " + status;
                dashboardService.addNotification(order.getUserId(), msg);
            });
    }

    public void cancelOrder(Integer orderId, Integer userId) {
        Order order = orderRepository.findAll().stream()
            .filter(o -> o.getId().equals(orderId) && o.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
            
        if (order.getStatus() != Order.Status.PENDING) {
            throw new IllegalStateException("Можно отменить только ожидающий заказ");
        }
        
        orderRepository.updateStatus(orderId, Order.Status.CANCELLED);
        dashboardService.addNotification(null, "Пользователь отменил заказ #" + orderId);
    }

    public List<StoredBook> getPhysicalBooksForSale() {
        return bookRepository.findByType(StoredBook.BookType.PHYSICAL);
    }
}
