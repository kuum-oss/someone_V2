package org.example.core.entity;

import java.time.LocalDateTime;

public class Notification {
    private Integer id;
    private Integer userId;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;

    public Notification(Integer id, Integer userId, String message, LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
