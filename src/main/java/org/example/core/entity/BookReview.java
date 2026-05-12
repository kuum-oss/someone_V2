package org.example.core.entity;

public class BookReview {
    private final int id;
    private final String reviewerName;
    private final String reviewText;
    private final String bookTitle;

    public BookReview(int id, String reviewerName, String reviewText) {
        this(id, reviewerName, reviewText, null);
    }

    public BookReview(int id, String reviewerName, String reviewText, String bookTitle) {
        this.id = id;
        this.reviewerName = reviewerName;
        this.reviewText = reviewText;
        this.bookTitle = bookTitle;
    }

    public int getId() {
        return id;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}
