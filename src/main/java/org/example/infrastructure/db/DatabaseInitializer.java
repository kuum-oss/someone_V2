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
                        "password VARCHAR(255) NOT NULL" +
                        ")";
                stmt.executeUpdate(createUsersTable);
                
                String createBooksTable = "CREATE TABLE IF NOT EXISTS books (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT NOT NULL," +
                        "title VARCHAR(255) NOT NULL," +
                        "file_path VARCHAR(512) NOT NULL," +
                        "original_name VARCHAR(255) NOT NULL," +
                        "file_size BIGINT NOT NULL DEFAULT 0," +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                        ")";
                stmt.executeUpdate(createBooksTable);
                
                // Пробуем добавить колонку file_size, если таблица уже существует без неё
                try {
                    stmt.executeUpdate("ALTER TABLE books ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0");
                } catch (SQLException e) {
                    // Игнорируем, если колонка уже существует
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
