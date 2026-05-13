//package org.example.infrastructure.repository;
//
//import org.example.core.entity.User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Tag;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Tag("integration")
//class JdbcUserRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {
//
//    private JdbcUserRepository userRepository;
//
//    @BeforeEach
//    void setUp() {
//        if (org.example.infrastructure.db.DatabaseConfig.getConnection() == null) {
//            org.junit.jupiter.api.Assumptions.assumeTrue(false, "No database connection available");
//        }
//        userRepository = new JdbcUserRepository();
//        try (java.sql.Connection conn = org.example.infrastructure.db.DatabaseConfig.getConnection();
//             java.sql.Statement stmt = conn.createStatement()) {
//            stmt.execute("DELETE FROM users");
//        } catch (java.sql.SQLException e) {
//            // Ignore if tables not yet created or connection fails
//        }
//    }
//
//    @Test
//    void testSaveAndFindById() {
//        User user = new User(null, "user@test.com", "pass123", true, 100);
//        User savedUser = userRepository.save(user);
//
//        assertNotNull(savedUser.getId());
//
//        Optional<User> found = userRepository.findById(savedUser.getId());
//        assertTrue(found.isPresent());
//        assertEquals("user@test.com", found.get().getEmail());
//        assertTrue(found.get().isAdmin());
//        assertEquals(100, found.get().getPoints());
//    }
//
//    @Test
//    void testFindByEmail() {
//        String email = "email@test.com";
//        userRepository.save(new User(null, email, "pass", false, 0));
//
//        Optional<User> found = userRepository.findByEmail(email);
//        assertTrue(found.isPresent());
//        assertEquals(email, found.get().getEmail());
//    }
//
//    @Test
//    void testUpdatePoints() {
//        User user = userRepository.save(new User(null, "points@test.com", "pass", false, 10));
//        userRepository.updatePoints(user.getId(), 50);
//
//        Optional<User> found = userRepository.findById(user.getId());
//        assertTrue(found.isPresent());
//        assertEquals(50, found.get().getPoints());
//    }
//}
