package org.example.core.service;

import org.example.core.entity.BookReview;
import org.example.core.entity.ReadingProgress;
import org.example.core.repository.ReadingRepository;
import java.util.List;
import java.util.Optional;

public class ReadingService {
    private final ReadingRepository readingRepository;

    public ReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public ReadingProgress saveProgress(ReadingProgress progress) {
        return readingRepository.saveOrUpdate(progress);
    }

    public Optional<ReadingProgress> getProgress(int userId, int bookId) {
        return readingRepository.findByUserIdAndBookId(userId, bookId);
    }

    public List<ReadingProgress> getUserReadingList(int userId, int limit) {
        return readingRepository.findByUserId(userId, limit);
    }

    public void updateNotes(int userId, int bookId, String notes) {
        ReadingProgress rp = readingRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(new ReadingProgress());
        rp.setUserId(userId);
        rp.setBookId(bookId);
        rp.setNotes(notes);
        readingRepository.saveOrUpdate(rp);
    }

    public void updateReview(int userId, int bookId, String review) {
        ReadingProgress rp = readingRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(new ReadingProgress());
        rp.setUserId(userId);
        rp.setBookId(bookId);
        rp.setReview(review);
        readingRepository.saveOrUpdate(rp);
    }

    public void updateSettings(int userId, int bookId, String settings) {
        ReadingProgress rp = readingRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(new ReadingProgress());
        rp.setUserId(userId);
        rp.setBookId(bookId);
        rp.setSettings(settings);
        readingRepository.saveOrUpdate(rp);
    }

    public void updateHighlights(int userId, int bookId, String highlights) {
        ReadingProgress rp = readingRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(new ReadingProgress());
        rp.setUserId(userId);
        rp.setBookId(bookId);
        rp.setHighlights(highlights);
        readingRepository.saveOrUpdate(rp);
    }

    public List<BookReview> getBookReviews(int bookId) {
        return readingRepository.findReviewsByBookId(bookId);
    }

    public List<BookReview> getAllReviews() {
        return readingRepository.findAllReviews();
    }

    public void deleteReview(int progressId) {
        readingRepository.deleteReview(progressId);
    }
}
