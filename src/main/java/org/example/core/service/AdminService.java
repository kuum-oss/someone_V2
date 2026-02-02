package org.example.core.service;

import org.example.core.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
    private final FileStorageService storageService;
    private final AuthService authService;
    private final org.example.core.repository.UserRepository userRepository;
    private final org.example.core.repository.BookRepository bookRepository;

    public AdminService(FileStorageService storageService, AuthService authService, 
                        org.example.core.repository.UserRepository userRepository,
                        org.example.core.repository.BookRepository bookRepository) {
        this.storageService = storageService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public boolean isAdmin() {
        return authService.isAuthenticated() && authService.getCurrentUser().isAdmin();
    }

    public void handleUserBanAndCleanup(Integer userId, String reason) {
        if (!isAdmin()) {
            throw new SecurityException("Access denied: Admin rights required.");
        }
        
        userRepository.findById(userId).ifPresent(user -> {
            // Add to blacklist
            userRepository.addToBlacklist(user.getEmail(), reason);
            
            // Delete all user books from storage and DB
            java.util.List<org.example.core.entity.StoredBook> userBooks = bookRepository.findByUserId(userId);
            for (org.example.core.entity.StoredBook book : userBooks) {
                storageService.deleteBook(book.getId());
            }
            
            // Delete user
            userRepository.deleteById(userId);
            
            // If current user banned themselves, logout
            if (authService.isAuthenticated() && authService.getCurrentUser().getId().equals(userId)) {
                authService.logout();
            }
            
            LOGGER.info("Admin {} banned user {} and cleaned up data. Reason: {}", 
                    authService.getCurrentUser() != null ? authService.getCurrentUser().getEmail() : "SYSTEM", 
                    user.getEmail(), reason);
        });
    }

    public java.util.List<org.example.core.entity.User> getAllUsers() {
        if (!isAdmin()) return java.util.Collections.emptyList();
        return userRepository.findAll();
    }

    public long getUserStorageUsage(Integer userId) {
        return bookRepository.getTotalSizeByUserId(userId);
    }

    public void uploadToShop(Book book) throws IOException {
        if (!isAdmin()) {
            throw new SecurityException("Access denied: Admin rights required.");
        }
        
        storageService.uploadBook(authService.getCurrentUser().getId(), book, true);
        LOGGER.info("Admin {} uploaded book '{}' to shop", authService.getCurrentUser().getEmail(), book.getTitle());
    }

    public void deleteBookFromShop(Integer bookId) {
        if (!isAdmin()) {
            throw new SecurityException("Access denied: Admin rights required.");
        }
        storageService.deleteBook(bookId);
        LOGGER.info("Admin {} deleted book with ID {} from shop", authService.getCurrentUser().getEmail(), bookId);
    }
}
