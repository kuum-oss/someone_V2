package org.example.infrastructure.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
    private static BasicDataSource dataSource;
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                LOGGER.error("Unable to find application.properties");
            } else {
                properties.load(input);
                // setupDataSource() called lazily in getConnection() or explicitly in setDataSource()
            }
        } catch (IOException e) {
            LOGGER.error("Error loading application.properties", e);
        }
    }

    private static void setupDataSource() {
        String baseUrl = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : properties.getProperty("db.url");
        String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : properties.getProperty("db.name");
        String user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : properties.getProperty("db.user");
        String pass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : properties.getProperty("db.password");

        dataSource = new BasicDataSource();
        dataSource.setUrl(baseUrl + dbName);
        dataSource.setUsername(user);
        dataSource.setPassword(pass);
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxOpenPreparedStatements(100);
        
        // Добавляем параметры для работы с большими файлами через JDBC
        dataSource.addConnectionProperty("maxAllowedPacket", "67108864"); // 64 MB
        dataSource.addConnectionProperty("useServerPrepStmts", "false");
        dataSource.addConnectionProperty("rewriteBatchedStatements", "true");
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            setupDataSource();
        }
        return dataSource.getConnection();
    }

    public static void setDataSource(BasicDataSource customDataSource) {
        if (dataSource != null && dataSource != customDataSource) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                LOGGER.error("Error closing old datasource", e);
            }
        }
        dataSource = customDataSource;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
