package org.example.application.controller;

import org.example.application.async.LibraryScanTask;
import org.example.application.async.OrganizeBooksTask;
import org.example.application.state.LibraryViewState;
import org.example.application.state.ViewMode;
import org.example.core.entity.Book;
import org.example.core.entity.StoredBook;
import org.example.core.service.AdminService;
import org.example.core.service.FileStorageService;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.usecase.GroupBooksUseCase;
import org.example.core.usecase.OrganizeBooksUseCase;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookLibraryController {
    private static final Logger logger = LoggerFactory.getLogger(BookLibraryController.class);
    private final ExtractMetadataUseCase extractMetadataUseCase;
    private final OrganizeBooksUseCase organizeBooksUseCase;
    private final GroupBooksUseCase groupBooksUseCase;
    private final FileStorageService storageService;
    private final AdminService adminService;
    private final org.example.core.service.OrderService orderService;
    private final LibraryViewState state;

    private SwingWorker<?, ?> currentWorker;

    public BookLibraryController(ExtractMetadataUseCase extractMetadataUseCase,
                                 OrganizeBooksUseCase organizeBooksUseCase,
                                 GroupBooksUseCase groupBooksUseCase,
                                 FileStorageService storageService,
                                 AdminService adminService,
                                 org.example.core.service.OrderService orderService,
                                 LibraryViewState state) {
        this.extractMetadataUseCase = extractMetadataUseCase;
        this.organizeBooksUseCase = organizeBooksUseCase;
        this.groupBooksUseCase = groupBooksUseCase;
        this.storageService = storageService;
        this.adminService = adminService;
        this.orderService = orderService;
        this.state = state;
    }

    public void scanLibrary(List<File> files, Consumer<List<Book>> onBooksFound, Consumer<Integer> onProgress, Runnable onDone) {
        cancelCurrentTask();
        LibraryScanTask task = new LibraryScanTask(files, extractMetadataUseCase, 
            books -> {
                state.getLocalBooks().addAll(books);
                onBooksFound.accept(books);
            }, 
            onProgress, 
            () -> {
                state.setLoading(false);
                onDone.run();
            }
        );
        state.setLoading(true);
        currentWorker = task;
        task.execute();
    }

    public void organizeBooks(List<Book> books, Path targetDir, Consumer<Integer> onProgress, Runnable onDone) {
        cancelCurrentTask();
        OrganizeBooksTask task = new OrganizeBooksTask(books, targetDir, organizeBooksUseCase, onProgress, onDone);
        currentWorker = task;
        task.execute();
    }

    public void uploadBook(Book book) throws IOException {
        storageService.uploadBook(state.getCurrentUser().getId(), book, false);
    }

    public void uploadToShop(Book book) throws IOException {
        adminService.uploadToShop(book);
    }

    public void buyBook(Book book, Runnable onDone) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                StoredBook sb = StoredBook.builder().id(book.getDatabaseId()).build();
                storageService.purchaseBook(state.getCurrentUser().getId(), sb);
                return null;
            }
            @Override
            protected void done() {
                onDone.run();
            }
        };
        worker.execute();
    }

    public void loadShopBooks(Consumer<List<Book>> onLoaded) {
        state.setLoading(true);
        SwingWorker<List<Book>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Book> doInBackground() {
                return storageService.getPublicBooks().stream()
                        .filter(sb -> sb.getBookType() == StoredBook.BookType.ELECTRONIC)
                        .map(sb -> Book.builder()
                                .title(sb.getTitle())
                                .author(sb.getAuthor())
                                .genre(sb.getGenre())
                                .year(sb.getYear())
                                .series(sb.getSeries())
                                .seriesIndex(sb.getSeriesIndex())
                                .language(sb.getLanguage())
                                .description(sb.getDescription())
                                .cover(sb.getCover())
                                .authorPhoto(sb.getAuthorPhoto())
                                .databaseId(sb.getId())
                                .isPublic(true)
                                .build())
                        .toList();
            }
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    state.setBooks(books);
                    state.setLoading(false);
                    onLoaded.accept(books);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while loading shop books", e);
                    state.setLoading(false);
                } catch (Exception e) {
                    logger.error("Failed to load shop books", e);
                    state.setLoading(false);
                }
            }
        };
        worker.execute();
    }

    public void loadPhysicalShopBooks(Consumer<List<Book>> onLoaded) {
        state.setLoading(true);
        SwingWorker<List<Book>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Book> doInBackground() {
                return orderService.getPhysicalBooksForSale().stream()
                        .map(sb -> Book.builder()
                                .title(sb.getTitle())
                                .author(sb.getAuthor())
                                .genre(sb.getGenre())
                                .year(sb.getYear())
                                .series(sb.getSeries())
                                .seriesIndex(sb.getSeriesIndex())
                                .language(sb.getLanguage())
                                .description(sb.getDescription())
                                .cover(sb.getCover())
                                .authorPhoto(sb.getAuthorPhoto())
                                .databaseId(sb.getId())
                                .format("PHYSICAL")
                                .isPublic(true)
                                .build())
                        .toList();
            }
            @Override
            protected void done() {
                try {
                    List<Book> books = get();
                    state.setBooks(books);
                    state.setLoading(false);
                    onLoaded.accept(books);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while loading physical shop books", e);
                    state.setLoading(false);
                } catch (Exception e) {
                    logger.error("Failed to load physical shop books", e);
                    state.setLoading(false);
                }
            }
        };
        worker.execute();
    }

    public void placeOrder(Book book, String seatNumber, java.time.LocalDateTime start, java.time.LocalDateTime end, Runnable onDone) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                orderService.placeOrder(state.getCurrentUser().getId(), book.getDatabaseId(), seatNumber, start, end);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    onDone.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while placing order", e);
                    JOptionPane.showMessageDialog(null, "Запрос был прерван", "Внимание", JOptionPane.WARNING_MESSAGE);
                } catch (Exception e) {
                    logger.error("Controller.placeOrder failed", e);
                    JOptionPane.showMessageDialog(null, "Ошибка при оформлении заказа: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    public void cancelCurrentTask() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
    }

    public void switchMode(ViewMode mode) {
        state.setMode(mode);
    }

    public String getPreview(Integer bookId) {
        return storageService.getPreview(bookId);
    }
}
