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
                setupDataSource();
            }
        } catch (IOException e) {
            LOGGER.error("Error loading application.properties", e);
        }
    }

    private static void setupDataSource() {
        String baseUrl = properties.getProperty("db.url");
        String dbName = properties.getProperty("db.name");
        String user = properties.getProperty("db.user");
        String pass = properties.getProperty("db.password");

        dataSource = new BasicDataSource();
        dataSource.setUrl(baseUrl + dbName);
        dataSource.setUsername(user);
        dataSource.setPassword(pass);
        dataSource.setMinIdle(5);
        dataSource.setMaxIdle(10);
        dataSource.setMaxOpenPreparedStatements(100);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
