//package org.example.infrastructure.repository;
//
//import org.example.core.entity.StoredBook;
//import org.example.core.entity.User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Tag;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Tag("integration")
//class JdbcBookRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {
//
//    private JdbcBookRepository bookRepository;
//    private JdbcUserRepository userRepository;
//    private User testUser;
//
//    @BeforeEach
//    void setUp() {
//        bookRepository = new JdbcBookRepository();
//        userRepository = new JdbcUserRepository();
//
//        try (java.sql.Connection conn = org.example.infrastructure.db.DatabaseConfig.getConnection();
//             java.sql.Statement stmt = conn.createStatement()) {
//            stmt.execute("DELETE FROM books");
//            stmt.execute("DELETE FROM users");
//        } catch (java.sql.SQLException e) {
//            e.printStackTrace();
//        }
//
//        // Create a test user because books are tied to users
//        testUser = userRepository.save(new User(null, "test@example.com", "password", false, 0));
//    }
//
//    @Test
//    void testSaveAndFindById() {
//        StoredBook book = StoredBook.builder()
//                .title("Integration Test Book")
//                .author("Test Author")
//                .userId(testUser.getId())
//                .isPublic(true)
//                .bookType(StoredBook.BookType.ELECTRONIC)
//                .build();
//
//        StoredBook savedBook = bookRepository.save(book);
//        assertNotNull(savedBook.getId());
//
//        Optional<StoredBook> foundBook = bookRepository.findById(savedBook.getId());
//        assertTrue(foundBook.isPresent());
//        assertEquals("Integration Test Book", foundBook.get().getTitle());
//        assertEquals("Test Author", foundBook.get().getAuthor());
//    }
//
//    @Test
//    void testUpdate() {
//        StoredBook book = StoredBook.builder()
//                .title("Original Title")
//                .userId(testUser.getId())
//                .build();
//        StoredBook savedBook = bookRepository.save(book);
//
//        savedBook.setTitle("Updated Title");
//        bookRepository.update(savedBook);
//
//        Optional<StoredBook> foundBook = bookRepository.findById(savedBook.getId());
//        assertTrue(foundBook.isPresent());
//        assertEquals("Updated Title", foundBook.get().getTitle());
//    }
//
//    @Test
//    void testDelete() {
//        StoredBook book = StoredBook.builder()
//                .title("To Be Deleted")
//                .userId(testUser.getId())
//                .build();
//        StoredBook savedBook = bookRepository.save(book);
//
//        bookRepository.deleteById(savedBook.getId());
//
//        Optional<StoredBook> foundBook = bookRepository.findById(savedBook.getId());
//        assertFalse(foundBook.isPresent());
//    }
//}
