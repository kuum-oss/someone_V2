package org.example.core.repository;

import org.example.core.entity.BookReview;
import org.example.core.entity.ReadingProgress;
import java.util.List;
import java.util.Optional;

public interface ReadingRepository {
    ReadingProgress saveOrUpdate(ReadingProgress progress);
    Optional<ReadingProgress> findByUserIdAndBookId(int userId, int bookId);
    List<ReadingProgress> findByUserId(int userId, int limit);
    List<BookReview> findReviewsByBookId(int bookId);
    List<BookReview> findAllReviews();
    void deleteReview(int progressId);
    void toggleFavorite(int userId, int bookId);
}
