package org.example.infrastructure.repository;

import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.infrastructure.db.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookRepository implements BookRepository {

    @Override
    public StoredBook save(StoredBook book) {
        String sql = "INSERT INTO books (user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, file_content, is_public, book_type, is_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, book.getUserId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getGenre());
            ps.setString(5, book.getYear());
            ps.setString(6, book.getSeries());
            if (book.getSeriesIndex() != null) {
                ps.setInt(7, book.getSeriesIndex());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setString(8, book.getLanguage());
            ps.setString(9, book.getDescription());
            ps.setBytes(10, book.getCover());
            ps.setBytes(11, book.getAuthorPhoto());
            if (book.getFilePath() != null) {
                ps.setString(12, book.getFilePath());
            } else {
                ps.setNull(12, Types.VARCHAR);
            }
            ps.setString(13, book.getOriginalName());
            ps.setLong(14, book.getFileSize());
            ps.setBytes(15, book.getFileContent());
            ps.setBoolean(16, book.isPublic());
            ps.setString(17, book.getBookType().name());
            ps.setBoolean(18, book.isAvailable());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return StoredBook.builder()
                            .id(rs.getInt(1))
                            .userId(book.getUserId())
                            .title(book.getTitle())
                            .author(book.getAuthor())
                            .genre(book.getGenre())
                            .year(book.getYear())
                            .series(book.getSeries())
                            .seriesIndex(book.getSeriesIndex())
                            .language(book.getLanguage())
                            .description(book.getDescription())
                            .cover(book.getCover())
                            .authorPhoto(book.getAuthorPhoto())
                            .filePath(book.getFilePath())
                            .originalName(book.getOriginalName())
                            .fileSize(book.getFileSize())
                            .isPublic(book.isPublic())
                            .bookType(book.getBookType())
                            .isAvailable(book.isAvailable())
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        }
        return book;
    }

    @Override
    public List<StoredBook> findByUserId(Integer userId) {
        List<StoredBook> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE user_id = ? AND is_public = FALSE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToStoredBook(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding books by user_id", e);
        }
        return books;
    }

    @Override
    public List<StoredBook> findPublicBooks() {
        List<StoredBook> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE is_public = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToStoredBook(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding public books", e);
        }
        return books;
    }

    private StoredBook mapResultSetToStoredBook(ResultSet rs, boolean includeContent) throws SQLException {
        StoredBook.Builder builder = StoredBook.builder()
                .id(rs.getInt("id"))
                .userId(rs.getInt("user_id"))
                .title(rs.getString("title"))
                .author(rs.getString("author"))
                .genre(rs.getString("genre"))
                .year(rs.getString("year"))
                .series(rs.getString("series"))
                .seriesIndex(rs.getObject("series_index") != null ? rs.getInt("series_index") : null)
                .language(rs.getString("language"))
                .description(rs.getString("description"))
                .cover(rs.getBytes("cover"))
                .authorPhoto(rs.getBytes("author_photo"))
                .filePath(rs.getString("file_path"))
                .originalName(rs.getString("original_name"))
                .fileSize(rs.getLong("file_size"))
                .fileContent(includeContent ? rs.getBytes("file_content") : null)
                .isPublic(rs.getBoolean("is_public"));

        // Безопасное чтение ENUM и BOOLEAN
        try {
            String typeStr = rs.getString("book_type");
            builder.bookType(typeStr != null ? StoredBook.BookType.valueOf(typeStr) : StoredBook.BookType.ELECTRONIC);
        } catch (Exception e) {
            builder.bookType(StoredBook.BookType.ELECTRONIC);
        }

        try {
            builder.isAvailable(rs.getBoolean("is_available"));
        } catch (Exception e) {
            builder.isAvailable(true);
        }

        return builder.build();
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

    @Override
    public byte[] getBookContent(Integer bookId) {
        String sql = "SELECT file_content FROM books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] content = rs.getBytes("file_content");
                    if (content == null) {
                        System.err.println("[DEBUG_LOG] file_content is NULL in DB for bookId: " + bookId);
                    }
                    return content;
                } else {
                    System.err.println("[DEBUG_LOG] No book found with id: " + bookId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting book content", e);
        }
        return null;
    }

    @Override
    public Optional<StoredBook> findById(Integer id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStoredBook(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding book by id", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting book by id", e);
        }
    }
    @Override
    public List<StoredBook> findByType(StoredBook.BookType type) {
        List<StoredBook> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE book_type = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToStoredBook(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding books by type", e);
        }
        return books;
    }

    @Override
    public long getTotalStorageSize() {
        String sql = "SELECT SUM(file_size) FROM books";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total storage size", e);
        }
        return 0;
    }

    @Override
    public long getTotalBookCount() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total book count", e);
        }
        return 0;
    }
}
