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
        String baseUrl = DatabaseConfig.getProperty("db.url");
        String dbName = DatabaseConfig.getProperty("db.name");
        String user = DatabaseConfig.getProperty("db.user");
        String pass = DatabaseConfig.getProperty("db.password");

        LOGGER.info("Attempting to initialize database at {} with user {}", baseUrl, user);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 1. Создание БД если не существует
            try (Connection conn = DriverManager.getConnection(baseUrl, user, pass);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                LOGGER.info("Database '{}' checked/created.", dbName);
            } catch (Exception e) {
                LOGGER.error("Failed to create database '{}'. Check if MySQL is running and credentials are correct.", dbName, e);
                throw e;
            }

            // 2. Создание таблиц
            // Используем прямое соединение с БД для создания таблиц, 
            // так как DatabaseConfig может не сработать если БД только что была создана
            String fullUrl = baseUrl + dbName;
            try (Connection conn = DriverManager.getConnection(fullUrl, user, pass);
                 Statement stmt = conn.createStatement()) {
                
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
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(createBooksTable);
                
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
                    "ALTER TABLE books ADD COLUMN author_photo MEDIUMBLOB"
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
                
                // Также попробуем увеличить max_allowed_packet на уровне сессии (для текущего соединения)
                try {
                    stmt.execute("SET GLOBAL max_allowed_packet=67108864");
                } catch (SQLException e) {
                    LOGGER.warn("Could not set GLOBAL max_allowed_packet: {}. This might require SUPER privileges.", e.getMessage());
                }
                
                LOGGER.info("Tables users and books checked/created.");
            } catch (Exception e) {
                LOGGER.error("Failed to create tables in database '{}'.", dbName, e);
                throw e;
            }

        } catch (Exception e) {
            LOGGER.error("Fatal error during database initialization", e);
            // Мы не выбрасываем RuntimeException здесь, чтобы приложение могло запуститься 
            // (возможно, пользователь захочет пользоваться им без БД для локальных книг)
            // Но пока оставим, как было, только с лучшим логированием.
            JOptionPane.showMessageDialog(null, 
                "Не удалось подключиться к базе данных MySQL.\n" +
                "Убедитесь, что MySQL запущен и пароль '74542474' верен.\n" +
                "Ошибка: " + e.getMessage(), 
                "Ошибка базы данных", JOptionPane.ERROR_MESSAGE);
        }
    }
}
