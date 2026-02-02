package org.example.infrastructure.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void initialize() {
        String baseUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DatabaseConfig.getProperty("db.url");
        String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : DatabaseConfig.getProperty("db.name");
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DatabaseConfig.getProperty("db.user");
        String pass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DatabaseConfig.getProperty("db.password");

        LOGGER.info("Attempting to initialize database at {} with user {}", baseUrl, user);

        int maxRetries = 5;
        int retryDelay = 3000; // 3 seconds
        boolean connected = false;

        for (int i = 0; i < maxRetries; i++) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Проверка доступности порта перед попыткой подключения (опционально, но полезно)
                
                // 1. Создание БД если не существует
                try (Connection conn = DriverManager.getConnection(baseUrl, user, pass);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                    LOGGER.info("Database '{}' checked/created.", dbName);
                }

                // 2. Создание таблиц
                String fullUrl = baseUrl + dbName;
                try (Connection conn = DriverManager.getConnection(fullUrl, user, pass);
                     Statement stmt = conn.createStatement()) {
                    
                    setupTables(stmt);
                    LOGGER.info("Tables checked/created successfully.");
                    connected = true;
                    break;
                }
            } catch (Exception e) {
                LOGGER.warn("Database initialization attempt {} failed: {}. Retrying in {}ms...", (i + 1), e.getMessage(), retryDelay);
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!connected) {
            LOGGER.error("Failed to initialize database after {} attempts.", maxRetries);
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(null,
                    "Не удалось подключиться к базе данных MySQL после нескольких попыток.\n" +
                    "Убедитесь, что MySQL запущен и доступен.\n" +
                    "Приложение может работать некорректно.",
                    "Ошибка базы данных", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void setupTables(Statement stmt) throws SQLException {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "email VARCHAR(255) NOT NULL UNIQUE," +
                "password VARCHAR(255) NOT NULL," +
                "is_admin BOOLEAN NOT NULL DEFAULT FALSE," +
                "points INT NOT NULL DEFAULT 5" +
                ")";
        stmt.executeUpdate(createUsersTable);

        String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "title VARCHAR(255) NOT NULL," +
                "author VARCHAR(255)," +
                "genre VARCHAR(100)," +
                "year VARCHAR(20)," +
                "series VARCHAR(255)," +
                "series_index INT," +
                "language VARCHAR(50)," +
                "description TEXT," +
                "cover MEDIUMBLOB," +
                "author_photo MEDIUMBLOB," +
                "file_path VARCHAR(512)," +
                "original_name VARCHAR(255) NOT NULL," +
                "file_size BIGINT NOT NULL DEFAULT 0," +
                "file_content LONGBLOB," +
                "is_public BOOLEAN NOT NULL DEFAULT FALSE," +
                "book_type ENUM('ELECTRONIC', 'PHYSICAL') NOT NULL DEFAULT 'ELECTRONIC'," +
                "is_available BOOLEAN NOT NULL DEFAULT TRUE," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(createBooksTable);

        String createBlacklistTable = "CREATE TABLE IF NOT EXISTS blacklist (" +
                "email VARCHAR(255) PRIMARY KEY," +
                "reason TEXT," +
                "banned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        stmt.executeUpdate(createBlacklistTable);

        String createNotificationsTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT," +
                "message TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "is_read BOOLEAN DEFAULT FALSE," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(createNotificationsTable);

        String createOrdersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "book_id INT NOT NULL," +
                "status ENUM('PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE" +
                ")";
        stmt.executeUpdate(createOrdersTable);

        // Пробуем добавить колонки, если таблица уже существует
        String[] columnsToAdd = {
            "ALTER TABLE books ADD COLUMN author VARCHAR(255)",
            "ALTER TABLE books ADD COLUMN genre VARCHAR(100)",
            "ALTER TABLE books ADD COLUMN year VARCHAR(20)",
            "ALTER TABLE books ADD COLUMN series VARCHAR(255)",
            "ALTER TABLE books ADD COLUMN series_index INT",
            "ALTER TABLE books ADD COLUMN language VARCHAR(50)",
            "ALTER TABLE books ADD COLUMN description TEXT",
            "ALTER TABLE books ADD COLUMN cover MEDIUMBLOB",
            "ALTER TABLE books ADD COLUMN author_photo MEDIUMBLOB",
            "ALTER TABLE books ADD COLUMN book_type ENUM('ELECTRONIC', 'PHYSICAL') NOT NULL DEFAULT 'ELECTRONIC'",
            "ALTER TABLE books ADD COLUMN is_available BOOLEAN NOT NULL DEFAULT TRUE"
        };

        for (String sql : columnsToAdd) {
            try {
                stmt.executeUpdate(sql);
            } catch (SQLException ignored) {}
        }

        try {
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT FALSE");
        } catch (SQLException ignored) {}

        try {
            stmt.executeUpdate("ALTER TABLE users ADD COLUMN points INT NOT NULL DEFAULT 5");
        } catch (SQLException ignored) {}

        try {
            stmt.executeUpdate("ALTER TABLE books ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT FALSE");
        } catch (SQLException ignored) {}

        try {
            stmt.executeUpdate("ALTER TABLE books MODIFY COLUMN file_path VARCHAR(512) NULL");
        } catch (SQLException ignored) {}

        try {
            stmt.executeUpdate("ALTER TABLE books MODIFY COLUMN file_content LONGBLOB");
        } catch (SQLException ignored) {}

        try {
            stmt.executeUpdate("ALTER TABLE books ADD COLUMN file_content LONGBLOB");
        } catch (SQLException ignored) {}

        try {
            stmt.execute("SET GLOBAL max_allowed_packet=67108864");
        } catch (SQLException e) {
            LOGGER.warn("Could not set GLOBAL max_allowed_packet: {}. This might require SUPER privileges.", e.getMessage());
        }
    }
}
