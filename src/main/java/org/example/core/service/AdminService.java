package org.example.core.service;

import org.example.core.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminService.class);
    private final FileStorageService storageService;
    private final AuthService authService;

    public AdminService(FileStorageService storageService, AuthService authService) {
        this.storageService = storageService;
        this.authService = authService;
    }

    public boolean isAdmin() {
        return authService.isAuthenticated() && authService.getCurrentUser().isAdmin();
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
