package org.example.infrastructure.repository;

import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.infrastructure.db.DatabaseConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcBookRepository implements BookRepository {
    private static final Logger logger = LoggerFactory.getLogger(JdbcBookRepository.class);

    @Override
    public StoredBook save(StoredBook book) {
        String sql = "INSERT INTO books (user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, file_content, is_public, book_type, is_available, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            
            // Если это электронная книга и цена 0, ставим 1 (fallback)
            int effectivePrice = book.getPrice();
            if (book.getBookType() == StoredBook.BookType.ELECTRONIC && effectivePrice <= 0) {
                effectivePrice = 1;
            }
            ps.setInt(19, effectivePrice);
            
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
                            .price(book.getPrice())
                            .build();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving book: " + e.getMessage(), e);
        }
        return book;
    }

    @Override
    public void update(StoredBook book) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, year = ?, series = ?, series_index = ?, language = ?, description = ?, cover = ?, author_photo = ?, is_public = ?, book_type = ?, is_available = ?, file_content = ?, price = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getGenre());
            ps.setString(4, book.getYear());
            ps.setString(5, book.getSeries());
            if (book.getSeriesIndex() != null) {
                ps.setInt(6, book.getSeriesIndex());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, book.getLanguage());
            ps.setString(8, book.getDescription());
            ps.setBytes(9, book.getCover());
            ps.setBytes(10, book.getAuthorPhoto());
            ps.setBoolean(11, book.isPublic());
            ps.setString(12, book.getBookType().name());
            ps.setBoolean(13, book.isAvailable());
            ps.setBytes(14, book.getFileContent());
            
            // Если это электронная книга и цена 0, ставим 1 (fallback)
            int effectivePrice = book.getPrice();
            if (book.getBookType() == StoredBook.BookType.ELECTRONIC && effectivePrice <= 0) {
                effectivePrice = 1;
            }
            ps.setInt(15, effectivePrice);
            ps.setInt(16, book.getId());
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("[DEBUG_LOG] No rows updated for book id: " + book.getId());
            } else {
                System.err.println("[DEBUG_LOG] Updated book id: " + book.getId() + ", rows affected: " + rows);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating book: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StoredBook> findByUserId(Integer userId) {
        List<StoredBook> books = new ArrayList<>();
        String sql = "SELECT id, user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, is_public, price, book_type, is_available FROM books WHERE user_id = ? AND is_public = FALSE";
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
        String sql = "SELECT id, user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, is_public, price, book_type, is_available FROM books WHERE is_public = TRUE";
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

    @Override
    public List<StoredBook> findOwnedBooksByUserId(Integer userId) {
        List<StoredBook> books = new ArrayList<>();
        // Книги, которые пользователь загрузил сам, ИЛИ которые он купил (есть в таблице orders)
        String sql = "SELECT b.id, b.user_id, b.title, b.author, b.genre, b.year, b.series, b.series_index, b.language, b.description, b.cover, b.author_photo, b.file_path, b.original_name, b.file_size, b.is_public, b.price, b.book_type, b.is_available FROM books b " +
                     "WHERE b.user_id = ? " +
                     "OR b.id IN (SELECT book_id FROM orders WHERE user_id = ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToStoredBook(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding owned books", e);
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
                .isPublic(rs.getBoolean("is_public"))
                .price(rs.getInt("price"));

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
        String sql = "SELECT id, user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, is_public, price, book_type, is_available FROM books WHERE id = ?";
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
        String sql = "SELECT id, user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, is_public, price, book_type, is_available FROM books WHERE book_type = ?";
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
    public List<StoredBook> findPublicBooks(String query, String genre, String language, String sort, int offset, int limit) {
        List<StoredBook> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, user_id, title, author, genre, year, series, series_index, language, description, cover, author_photo, file_path, original_name, file_size, is_public, price, book_type, is_available FROM books WHERE is_public = true");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?))");
            params.add("%" + query.trim() + "%");
            params.add("%" + query.trim() + "%");
        }
        if (genre != null && !genre.isBlank()) {
            sql.append(" AND genre = ?");
            params.add(genre);
        }
        if (language != null && !language.isBlank()) {
            sql.append(" AND language = ?");
            params.add(language);
        }

        if (sort != null) {
            switch (sort) {
                case "title" -> sql.append(" ORDER BY title ASC");
                case "author" -> sql.append(" ORDER BY author ASC");
                case "newest" -> sql.append(" ORDER BY id DESC");
                default -> sql.append(" ORDER BY title ASC");
            }
        } else {
            sql.append(" ORDER BY title ASC");
        }

        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (java.sql.Connection conn = org.example.infrastructure.db.DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToStoredBook(rs, false));
                }
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error finding public books", e);
        }
        return books;
    }

    @Override
    public long countPublicBooks(String query, String genre, String language) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books WHERE is_public = true");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?))");
            params.add("%" + query.trim() + "%");
            params.add("%" + query.trim() + "%");
        }
        if (genre != null && !genre.isBlank()) {
            sql.append(" AND genre = ?");
            params.add(genre);
        }
        if (language != null && !language.isBlank()) {
            sql.append(" AND language = ?");
            params.add(language);
        }

        try (java.sql.Connection conn = org.example.infrastructure.db.DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error counting public books", e);
        }
        return 0;
    }

    @Override
    public List<String> findAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM books WHERE genre IS NOT NULL AND genre != '' ORDER BY genre";
        try (java.sql.Connection conn = org.example.infrastructure.db.DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error finding all genres", e);
        }
        return genres;
    }

    @Override
    public List<String> findAllLanguages() {
        List<String> languages = new ArrayList<>();
        String sql = "SELECT DISTINCT language FROM books WHERE language IS NOT NULL AND language != '' ORDER BY language";
        try (java.sql.Connection conn = org.example.infrastructure.db.DatabaseConfig.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                languages.add(rs.getString("language"));
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error finding all languages", e);
        }
        return languages;
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
