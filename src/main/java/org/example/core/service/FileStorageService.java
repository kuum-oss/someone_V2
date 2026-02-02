package org.example.core.service;

import org.example.core.entity.Book;
import org.example.core.entity.Order;
import org.example.core.entity.StoredBook;
import org.example.core.repository.BookRepository;
import org.example.core.repository.OrderRepository;
import org.example.core.usecase.port.MetadataGateway;
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
    private final MetadataGateway metadataGateway;

    private final OrderRepository orderRepository;

    public FileStorageService(BookRepository bookRepository, AuthService authService, MetadataGateway metadataGateway, OrderRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.authService = authService;
        this.metadataGateway = metadataGateway;
        this.orderRepository = orderRepository;
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
        StoredBook.BookType type = book.getFormat().equals("PHYSICAL") ? StoredBook.BookType.PHYSICAL : StoredBook.BookType.ELECTRONIC;
        
        long fileSize = 0;
        byte[] content = null;
        String fileName = "PHYSICAL";
        String filePath = null;

        if (type == StoredBook.BookType.ELECTRONIC) {
            Path sourceFile = book.getFilePath();
            if (sourceFile == null || !Files.exists(sourceFile)) {
                throw new IOException("Source file does not exist or path is null");
            }
            fileSize = Files.size(sourceFile);
            if (!isPublic) {
                long currentTotal = bookRepository.getTotalSizeByUserId(userId);
                if (currentTotal + fileSize > MAX_QUOTA) {
                    throw new IOException("Storage quota exceeded. You can't upload more than 5 GB.");
                }
            }
            content = Files.readAllBytes(sourceFile);
            fileName = sourceFile.getFileName().toString();
            filePath = isPublic ? null : sourceFile.toAbsolutePath().toString();
        }

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
                .bookType(type)
                .format(book.getFormat())
                .build();
        
        bookRepository.save(sb);
        if (!isPublic && type == StoredBook.BookType.ELECTRONIC) {
            int currentPoints = authService.getCurrentUser().getPoints();
            authService.updateCurrentUserPoints(currentPoints + 1);
        }
        LOGGER.info("Book '{}' (type={}) with metadata uploaded to database (public={}) for user {}", 
                book.getTitle(), type, isPublic, userId);
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

        // Вместо дублирования книги в таблице books, мы просто создаем запись в orders,
        // чтобы пользователь владел оригинальной публичной книгой.
        orderRepository.save(new Order(null, userId, sourceBook.getId(), Order.Status.DELIVERED, java.time.LocalDateTime.now()));
        LOGGER.info("Book '{}' purchased by user {}", sourceBook.getTitle(), userId);
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

    public String getPreview(Integer bookId) {
        byte[] content = bookRepository.getBookContent(bookId);
        if (content == null) {
            return "Контент книги не найден.";
        }
        return metadataGateway.extractTextPreview(content, 5000); // Ограничим 5000 символов для превью
    }
}
