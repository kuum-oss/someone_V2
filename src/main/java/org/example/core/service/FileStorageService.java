package org.example.core.service;

import org.example.core.entity.Book;
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
    private final AuthService authService;

    public FileStorageService(BookRepository bookRepository, AuthService authService) {
        this.bookRepository = bookRepository;
        this.authService = authService;
    }

    public void uploadBook(Integer userId, Path sourceFile, String title) throws IOException {
        uploadBook(userId, sourceFile, title, false);
    }

    public void uploadBook(Integer userId, Path sourceFile, String title, boolean isPublic) throws IOException {
        long fileSize = Files.size(sourceFile);
        if (!isPublic) {
            long currentTotal = bookRepository.getTotalSizeByUserId(userId);
            if (currentTotal + fileSize > MAX_QUOTA) {
                throw new IOException("Storage quota exceeded. You can't upload more than 5 GB.");
            }
        }

        byte[] content = Files.readAllBytes(sourceFile);
        String fileName = sourceFile.getFileName().toString();
        String filePath = isPublic ? null : sourceFile.toAbsolutePath().toString();
        
        StoredBook book = new StoredBook(null, userId, title, filePath, fileName, fileSize, content, isPublic);
        bookRepository.save(book);
        if (!isPublic) {
            int currentPoints = authService.getCurrentUser().getPoints();
            authService.updateCurrentUserPoints(currentPoints + 1);
        }
        LOGGER.info("Book '{}' uploaded to database (public={}) for user {}", title, isPublic, userId);
    }

    public void uploadBook(Integer userId, Book book, boolean isPublic) throws IOException {
        Path sourceFile = book.getFilePath();
        if (sourceFile == null || !Files.exists(sourceFile)) {
            throw new IOException("Source file does not exist or path is null");
        }
        
        long fileSize = Files.size(sourceFile);
        if (!isPublic) {
            long currentTotal = bookRepository.getTotalSizeByUserId(userId);
            if (currentTotal + fileSize > MAX_QUOTA) {
                throw new IOException("Storage quota exceeded. You can't upload more than 5 GB.");
            }
        }

        byte[] content = Files.readAllBytes(sourceFile);
        String fileName = sourceFile.getFileName().toString();
        String filePath = isPublic ? null : sourceFile.toAbsolutePath().toString();

        StoredBook sb = StoredBook.builder()
                .userId(userId)
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .year(book.getYear())
                .series(book.getSeries())
                .seriesIndex(book.getSeriesIndex())
                .language(book.getLanguage())
                .description(book.getDescription())
                .cover(book.getCover())
                .authorPhoto(book.getAuthorPhoto())
                .filePath(filePath)
                .originalName(fileName)
                .fileSize(fileSize)
                .fileContent(content)
                .isPublic(isPublic)
                .build();
        
        bookRepository.save(sb);
        if (!isPublic) {
            int currentPoints = authService.getCurrentUser().getPoints();
            authService.updateCurrentUserPoints(currentPoints + 1);
        }
        LOGGER.info("Book '{}' with metadata uploaded to database (public={}) for user {}", book.getTitle(), isPublic, userId);
    }

    public List<StoredBook> getUserBooks(Integer userId) {
        return bookRepository.findByUserId(userId);
    }

    public List<StoredBook> getPublicBooks() {
        return bookRepository.findPublicBooks();
    }

    public void purchaseBook(Integer userId, StoredBook publicBook) {
        org.example.core.entity.StoredBook sourceBook = bookRepository.findById(publicBook.getId())
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + publicBook.getId()));

        byte[] content = bookRepository.getBookContent(sourceBook.getId());
        
        StoredBook userBook = StoredBook.builder()
                .userId(userId)
                .title(sourceBook.getTitle())
                .author(sourceBook.getAuthor())
                .genre(sourceBook.getGenre())
                .year(sourceBook.getYear())
                .series(sourceBook.getSeries())
                .seriesIndex(sourceBook.getSeriesIndex())
                .language(sourceBook.getLanguage())
                .description(sourceBook.getDescription())
                .cover(sourceBook.getCover())
                .authorPhoto(sourceBook.getAuthorPhoto())
                .filePath(null)
                .originalName(sourceBook.getOriginalName())
                .fileSize(sourceBook.getFileSize())
                .fileContent(content)
                .isPublic(false)
                .build();
                
        bookRepository.save(userBook);
    }
    
    public long getUserStorageUsage(Integer userId) {
        return bookRepository.getTotalSizeByUserId(userId);
    }
    
    public byte[] getBookContent(Integer bookId) {
        return bookRepository.getBookContent(bookId);
    }

    public void deleteBook(Integer bookId) {
        bookRepository.deleteById(bookId);
    }
}
