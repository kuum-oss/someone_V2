package org.example.core.service;

import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);
    public static final long MAX_QUOTA = 5 * 1024 * 1024 * 1024L; // 5 GB
    private final BookRepository bookRepository;

    public FileStorageService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public void uploadBook(Integer userId, Path sourceFile, String title) throws IOException {
        long fileSize = Files.size(sourceFile);
        long currentTotal = bookRepository.getTotalSizeByUserId(userId);
        
        if (currentTotal + fileSize > MAX_QUOTA) {
            throw new IOException("Storage quota exceeded. You can't upload more than 5 GB.");
        }

        byte[] content = Files.readAllBytes(sourceFile);
        String fileName = sourceFile.getFileName().toString();
        String filePath = sourceFile.toAbsolutePath().toString();
        
        StoredBook book = new StoredBook(null, userId, title, filePath, fileName, fileSize, content);
        bookRepository.save(book);
        LOGGER.info("Book '{}' uploaded to database for user {}", title, userId);
    }

    public List<StoredBook> getUserBooks(Integer userId) {
        return bookRepository.findByUserId(userId);
    }
    
    public long getUserStorageUsage(Integer userId) {
        return bookRepository.getTotalSizeByUserId(userId);
    }
    
    public byte[] getBookContent(Integer bookId) {
        return bookRepository.getBookContent(bookId);
    }
}
