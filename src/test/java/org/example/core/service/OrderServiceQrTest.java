package org.example.core.service;

import org.example.core.entity.Order;
import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.core.repository.NotificationRepository;
import org.example.core.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceQrTest {

    private OrderRepository orderRepository;
    private BookRepository bookRepository;
    private NotificationRepository notificationRepository;
    private AdminDashboardService dashboardService;
    private QrCodeService qrCodeService;
    private EmailService emailService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        bookRepository = mock(BookRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        dashboardService = new AdminDashboardService(bookRepository, notificationRepository);
        qrCodeService = new QrCodeService();
        emailService = new EmailService();

        orderService = new OrderService(orderRepository, bookRepository, dashboardService, qrCodeService, emailService);
    }

    @Test
    @DisplayName("Should successfully place physical book order, generate SHA-256 QR token and PNG QR bytes")
    void testPlaceOrderPhysicalBookSuccess() {
        Integer userId = 10;
        Integer bookId = 100;
        String seatNumber = "M5";
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        StoredBook physicalBook = StoredBook.builder()
                .id(bookId)
                .userId(userId)
                .title("Test Physical Book")
                .author("Author")
                .genre("Genre")
                .bookType(StoredBook.BookType.PHYSICAL)
                .isAvailable(true)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(physicalBook));
        when(orderRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(Collections.emptyList());

        Order savedOrder = new Order(1, userId, bookId, Order.Status.PENDING, LocalDateTime.now(), seatNumber, start, end);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order fullOrder = new Order(1, userId, bookId, Order.Status.PENDING, LocalDateTime.now(), seatNumber, start, end);
        fullOrder.setBookTitle("Test Physical Book");
        fullOrder.setUserEmail("user@example.com");
        fullOrder.setQrToken(qrCodeService.generateOrderToken(1));
        when(orderRepository.findById(1)).thenReturn(fullOrder);

        Order result = orderService.placeOrder(userId, bookId, seatNumber, start, end);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getQrToken());
        assertEquals(64, result.getQrToken().length(), "QR Token should be 64-character SHA-256 hex string");

        assertNotNull(result.getQrCode(), "Generated PNG QR code bytes should not be null");
        assertTrue(result.getQrCode().length > 0, "Generated PNG QR code bytes should not be empty");
        assertEquals((byte) 0x89, result.getQrCode()[0], "Magic byte of PNG image should match");

        verify(orderRepository).updateQrToken(eq(1), anyString());
        verify(notificationRepository).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when trying to order non-physical book")
    void testPlaceOrderNonPhysicalBook() {
        Integer userId = 10;
        Integer bookId = 101;

        StoredBook electronicBook = StoredBook.builder()
                .id(bookId)
                .userId(userId)
                .title("E-Book")
                .author("Author")
                .genre("Genre")
                .bookType(StoredBook.BookType.ELECTRONIC)
                .isAvailable(true)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(electronicBook));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.placeOrder(userId, bookId, "M1", LocalDateTime.now(), LocalDateTime.now().plusHours(2))
        );

        assertEquals("Only physical books can be ordered", ex.getMessage());
    }

    @Test
    @DisplayName("Should retrieve QR code bytes for valid order")
    void testGetOrderQrCode() {
        Integer orderId = 5;
        Order order = new Order(orderId, 1, 100, Order.Status.PENDING, LocalDateTime.now());
        String token = qrCodeService.generateOrderToken(orderId);
        order.setQrToken(token);

        when(orderRepository.findById(orderId)).thenReturn(order);

        byte[] resultBytes = orderService.getOrderQrCode(orderId);

        assertNotNull(resultBytes);
        assertTrue(resultBytes.length > 0);
        assertEquals((byte) 0x89, resultBytes[0]);
    }
}
