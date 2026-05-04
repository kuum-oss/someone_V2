package org.example.core.entity;

import java.time.LocalDateTime;

public class Order {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Status status;
    private LocalDateTime createdAt;
    private String seatNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

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

    public Order(Integer id, Integer userId, Integer bookId, Status status, LocalDateTime createdAt, String seatNumber, LocalDateTime startTime, LocalDateTime endTime) {
        this(id, userId, bookId, status, createdAt);
        this.seatNumber = seatNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Integer getBookId() { return bookId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
}
