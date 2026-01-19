package org.example.infrastructure.repository;

import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.infrastructure.db.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookRepository implements BookRepository {

    @Override
    public StoredBook save(StoredBook book) {
        String sql = "INSERT INTO books (user_id, title, file_path, original_name, file_size) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, book.getUserId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getFilePath());
            ps.setString(4, book.getOriginalName());
            ps.setLong(5, book.getFileSize());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new StoredBook(rs.getInt(1), book.getUserId(), book.getTitle(), book.getFilePath(), book.getOriginalName(), book.getFileSize());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving book", e);
        }
        return book;
    }

    @Override
    public List<StoredBook> findByUserId(Integer userId) {
        List<StoredBook> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new StoredBook(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("file_path"),
                            rs.getString("original_name"),
                            rs.getLong("file_size")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding books by user_id", e);
        }
        return books;
    }

    @Override
    public long getTotalSizeByUserId(Integer userId) {
        String sql = "SELECT SUM(file_size) FROM books WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating total storage size", e);
        }
        return 0;
    }
}
