package org.example.core.service;

import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.infrastructure.db.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class FileStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);
    public static final long MAX_QUOTA = 5 * 1024 * 1024 * 1024L; // 5 GB
    private final String baseStoragePath;
    private final BookRepository bookRepository;

    public FileStorageService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
        this.baseStoragePath = DatabaseConfig.getProperty("storage.path", "storage");
    }

    public void uploadBook(Integer userId, Path sourceFile, String title) throws IOException {
        long fileSize = Files.size(sourceFile);
        long currentTotal = bookRepository.getTotalSizeByUserId(userId);
        
        if (currentTotal + fileSize > MAX_QUOTA) {
            throw new IOException("Storage quota exceeded. You can't upload more than 5 GB.");
        }

        Path userDir = Paths.get(baseStoragePath, "user_" + userId);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        String fileName = sourceFile.getFileName().toString();
        Path targetPath = userDir.resolve(fileName);
        
        // Предотвращение перезаписи при конфликте имен
        if (Files.exists(targetPath)) {
            String nameWithoutExt = fileName;
            String ext = "";
            int dotIdx = fileName.lastIndexOf('.');
            if (dotIdx > 0) {
                nameWithoutExt = fileName.substring(0, dotIdx);
                ext = fileName.substring(dotIdx);
            }
            targetPath = userDir.resolve(nameWithoutExt + "_" + System.currentTimeMillis() + ext);
        }

        Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        StoredBook book = new StoredBook(null, userId, title, targetPath.toString(), fileName, fileSize);
        bookRepository.save(book);
        LOGGER.info("Book '{}' uploaded for user {}", title, userId);
    }

    public List<StoredBook> getUserBooks(Integer userId) {
        return bookRepository.findByUserId(userId);
    }
    
    public long getUserStorageUsage(Integer userId) {
        return bookRepository.getTotalSizeByUserId(userId);
    }
    
    public Path getBookPath(StoredBook book) {
        return Paths.get(book.getFilePath());
    }
}
