package org.example.application.async;

import org.example.core.entity.Book;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.util.BookFileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibraryScanTaskTest {

    @TempDir
    Path tempDir;

    private ExtractMetadataUseCase extractMetadataUseCase;

    @BeforeEach
    void setUp() {
        extractMetadataUseCase = mock(ExtractMetadataUseCase.class);
        when(extractMetadataUseCase.execute(any(Path.class))).thenAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            return Book.builder().title(path.getFileName().toString()).filePath(path).build();
        });
    }

    @Test
    void testRecursiveScanning() throws IOException, InterruptedException {
        // Create folder structure:
        // tempDir/book1.epub
        // tempDir/subdir/book2.pdf
        // tempDir/subdir/not-a-book.txt
        
        Files.createFile(tempDir.resolve("book1.epub"));
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectories(subDir);
        Files.createFile(subDir.resolve("book2.pdf"));
        Files.createFile(subDir.resolve("not-a-book.txt"));

        List<File> filesToScan = List.of(tempDir.toFile());
        List<Book> foundBooks = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger progress = new AtomicInteger(0);

        LibraryScanTask task = new LibraryScanTask(
                filesToScan,
                extractMetadataUseCase,
                foundBooks::addAll,
                progress::set,
                latch::countDown
        );

        // We can't easily run SwingWorker in tests because publish/process needs Event Dispatch Thread
        // But we can test the internal scan logic if we make it accessible or mock the task.
        // For simplicity, let's just run it and wait for done()
        
        task.execute();
        
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Scan task did not finish in time");
        
        // Check results
        assertEquals(2, foundBooks.size(), "Should have found 2 books");
        boolean foundBook1 = foundBooks.stream().anyMatch(b -> b.getTitle().equals("book1.epub"));
        boolean foundBook2 = foundBooks.stream().anyMatch(b -> b.getTitle().equals("book2.pdf"));
        
        assertTrue(foundBook1, "book1.epub should be found");
        assertTrue(foundBook2, "book2.pdf should be found");
        assertFalse(foundBooks.stream().anyMatch(b -> b.getTitle().equals("not-a-book.txt")), "TXT should be ignored");
    }
}
