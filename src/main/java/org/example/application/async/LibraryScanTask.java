package org.example.application.async;

import org.example.core.entity.Book;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.util.BookFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class LibraryScanTask extends SwingWorker<Void, Book> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryScanTask.class);
    private final List<File> files;
    private final ExtractMetadataUseCase extractMetadataUseCase;
    private final Consumer<List<Book>> onBooksFound;
    private final Consumer<Integer> onProgress;
    private final Runnable onDone;
    private int processedCount = 0;

    public LibraryScanTask(List<File> files, ExtractMetadataUseCase extractMetadataUseCase,
                           Consumer<List<Book>> onBooksFound,
                           Consumer<Integer> onProgress, Runnable onDone) {
        this.files = files;
        this.extractMetadataUseCase = extractMetadataUseCase;
        this.onBooksFound = onBooksFound;
        this.onProgress = onProgress;
        this.onDone = onDone;
    }

    @Override
    protected Void doInBackground() {
        try {
            scan(files);
        } catch (Exception e) {
            LOGGER.error("Critical error during library scanning", e);
        }
        return null;
    }

    private void scan(List<File> list) {
        for (File f : list) {
            if (isCancelled()) return;
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null) {
                    scan(Arrays.asList(children));
                }
            } else if (BookFileUtils.isBookFile(f.toPath())) {
                try {
                    Book book = extractMetadataUseCase.execute(f.toPath());
                    publish(book);
                } catch (Exception e) {
                    LOGGER.warn("Failed to extract metadata from file: {}", f.getAbsolutePath(), e);
                }
            }
        }
    }

    @Override
    protected void process(List<Book> chunks) {
        processedCount += chunks.size();
        onBooksFound.accept(chunks);
        onProgress.accept(processedCount);
    }

    @Override
    protected void done() {
        onDone.run();
    }
}
