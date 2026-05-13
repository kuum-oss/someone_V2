package org.example.infrastructure.repository;

import org.apache.commons.dbcp2.BasicDataSource;
import org.example.infrastructure.db.DatabaseConfig;
import org.example.infrastructure.db.DatabaseInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

public abstract class BaseRepositoryIntegrationTest {

    protected static MySQLContainer<?> mysql;

    @BeforeAll
    static void setup() {
        if (!isDockerAvailable()) {
            // Fallback to H2 for environments without Docker
            setupH2();
            return;
        }

        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("library_test")
                .withUsername("test")
                .withPassword("test");
        mysql.start();

        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(mysql.getJdbcUrl());
        ds.setUsername(mysql.getUsername());
        ds.setPassword(mysql.getPassword());
        ds.setMinIdle(1);
        ds.setMaxIdle(5);
        
        DatabaseConfig.setDataSource(ds);
        DatabaseInitializer.initialize();
    }

    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    private static void setupH2() {
        BasicDataSource ds = new BasicDataSource();
        // Используем H2 в режиме совместимости с MySQL
        // Добавляем DB_CLOSE_DELAY=-1 чтобы база не удалялась при закрытии одного из соединений в пуле
        // Используем фиксированное имя БД, чтобы все тесты работали с одной и той же базой
        ds.setUrl("jdbc:h2:mem:library_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        ds.setDriverClassName("org.h2.Driver");
        ds.setUsername("sa");
        ds.setPassword("");
        
        DatabaseConfig.setDataSource(ds);
        
        // Гарантируем, что инициализация таблиц произойдет только один раз для этой БД
        synchronized (BaseRepositoryIntegrationTest.class) {
            try (java.sql.Connection conn = ds.getConnection();
                 java.sql.ResultSet rs = conn.getMetaData().getTables(null, null, "USERS", null)) {
                if (!rs.next()) {
                    DatabaseInitializer.initialize();
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterAll
    static void tearDown() {
        if (mysql != null && mysql.isRunning()) {
            mysql.stop();
        }
    }
}
