package org.example.infrastructure.repository;

import org.example.core.entity.BookReview;
import org.example.core.entity.ReadingProgress;
import org.example.core.repository.ReadingRepository;
import org.example.infrastructure.db.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcReadingRepository implements ReadingRepository {
    private static final Logger logger = LoggerFactory.getLogger(JdbcReadingRepository.class);

    @Override
    public ReadingProgress saveOrUpdate(ReadingProgress progress) {
        String sql = "INSERT INTO reading_progress (user_id, book_id, current_page, total_pages, reading_speed, notes, review, settings, highlights, is_favorite) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE current_page = VALUES(current_page), total_pages = VALUES(total_pages), " +
                     "reading_speed = VALUES(reading_speed), notes = VALUES(notes), review = VALUES(review), " +
                     "settings = VALUES(settings), highlights = VALUES(highlights), is_favorite = VALUES(is_favorite)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, progress.getUserId());
            ps.setInt(2, progress.getBookId());
            ps.setInt(3, progress.getCurrentPage());
            ps.setInt(4, progress.getTotalPages());
            ps.setDouble(5, progress.getReadingSpeed());
            ps.setString(6, progress.getNotes());
            ps.setString(7, progress.getReview());
            ps.setString(8, progress.getSettings());
            ps.setString(9, progress.getHighlights());
            ps.setBoolean(10, progress.isFavorite());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    progress.setId(rs.getInt(1));
                }
            }
            return progress;
        } catch (SQLException e) {
            logger.error("Error saving/updating reading progress", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ReadingProgress> findByUserIdAndBookId(int userId, int bookId) {
        String sql = "SELECT * FROM reading_progress WHERE user_id = ? AND book_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reading progress", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ReadingProgress> findByUserId(int userId, int limit) {
        List<ReadingProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM reading_progress WHERE user_id = ? ORDER BY last_read DESC LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reading progress for user", e);
        }
        return list;
    }

    @Override
    public List<BookReview> findReviewsByBookId(int bookId) {
        List<BookReview> reviews = new ArrayList<>();
        String sql = "SELECT rp.id, u.email, rp.review FROM reading_progress rp " +
                     "JOIN users u ON rp.user_id = u.id " +
                     "WHERE rp.book_id = ? AND rp.review IS NOT NULL AND rp.review != ''";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new BookReview(rs.getInt("id"), rs.getString("email"), rs.getString("review")));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding reviews for book", e);
        }
        return reviews;
    }

    @Override
    public List<BookReview> findAllReviews() {
        List<BookReview> reviews = new ArrayList<>();
        String sql = "SELECT rp.id, u.email, rp.review, b.title FROM reading_progress rp " +
                     "JOIN users u ON rp.user_id = u.id " +
                     "JOIN books b ON rp.book_id = b.id " +
                     "WHERE rp.review IS NOT NULL AND rp.review != '' " +
                     "ORDER BY rp.last_read DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reviews.add(new BookReview(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("review"),
                    rs.getString("title")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error finding all reviews", e);
        }
        return reviews;
    }

    @Override
    public void deleteReview(int progressId) {
        String sql = "UPDATE reading_progress SET review = NULL WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, progressId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting review", e);
        }
    }

    @Override
    public void toggleFavorite(int userId, int bookId) {
        String sql = "INSERT INTO reading_progress (user_id, book_id, is_favorite) " +
                     "VALUES (?, ?, TRUE) " +
                     "ON DUPLICATE KEY UPDATE is_favorite = NOT is_favorite";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error toggling favorite status", e);
            throw new RuntimeException(e);
        }
    }

    private ReadingProgress mapRow(ResultSet rs) throws SQLException {
        ReadingProgress rp = new ReadingProgress();
        rp.setId(rs.getInt("id"));
        rp.setUserId(rs.getInt("user_id"));
        rp.setBookId(rs.getInt("book_id"));
        rp.setCurrentPage(rs.getInt("current_page"));
        rp.setTotalPages(rs.getInt("total_pages"));
        rp.setReadingSpeed(rs.getDouble("reading_speed"));
        rp.setNotes(rs.getString("notes"));
        rp.setReview(rs.getString("review"));
        rp.setSettings(rs.getString("settings"));
        rp.setHighlights(rs.getString("highlights"));
        rp.setFavorite(rs.getBoolean("is_favorite"));
        rp.setLastRead(rs.getTimestamp("last_read").toLocalDateTime());
        return rp;
    }
}
