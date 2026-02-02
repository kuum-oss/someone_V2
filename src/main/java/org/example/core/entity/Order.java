package org.example.core.entity;

import java.time.LocalDateTime;

public class Order {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Status status;
    private LocalDateTime createdAt;
    
    // For UI display
    private String userEmail;
    private String bookTitle;

    public enum Status {
        PENDING, SHIPPED, DELIVERED, CANCELLED
    }

    public Order(Integer id, Integer userId, Integer bookId, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Integer getBookId() { return bookId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
}
