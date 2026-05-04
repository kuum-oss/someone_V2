package org.example.core.entity;

import java.time.LocalDateTime;

public class ReadingProgress {
    private int id;
    private int userId;
    private int bookId;
    private int currentPage;
    private int totalPages;
    private double readingSpeed;
    private String notes;
    private String review;
    private String settings; // JSON string for reader settings
    private String highlights; // JSON string for highlights/comments
    private LocalDateTime lastRead;

    public ReadingProgress() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public double getReadingSpeed() { return readingSpeed; }
    public void setReadingSpeed(double readingSpeed) { this.readingSpeed = readingSpeed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }

    public String getHighlights() { return highlights; }
    public void setHighlights(String highlights) { this.highlights = highlights; }

    public LocalDateTime getLastRead() { return lastRead; }
    public void setLastRead(LocalDateTime lastRead) { this.lastRead = lastRead; }
}
