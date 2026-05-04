package org.example.application.async;

import org.example.core.entity.Book;
import org.example.core.usecase.OrganizeBooksUseCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class OrganizeBooksTask extends SwingWorker<Void, Integer> {
    private static final Logger logger = LoggerFactory.getLogger(OrganizeBooksTask.class);
    private final List<Book> books;
    private final Path targetDir;
    private final OrganizeBooksUseCase organizeBooksUseCase;
    private final Consumer<Integer> onProgress;
    private final Runnable onDone;

    public OrganizeBooksTask(List<Book> books, Path targetDir, OrganizeBooksUseCase organizeBooksUseCase,
                             Consumer<Integer> onProgress, Runnable onDone) {
        this.books = books;
        this.targetDir = targetDir;
        this.organizeBooksUseCase = organizeBooksUseCase;
        this.onProgress = onProgress;
        this.onDone = onDone;
    }

    @Override
    protected Void doInBackground() {
        int i = 0;
        for (Book book : books) {
            if (isCancelled()) break;
            try {
                organizeBooksUseCase.execute(book, targetDir);
                publish(++i);
            } catch (IOException e) {
                logger.error("Failed to organize book: {}", book.getTitle(), e);
            }
        }
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        onProgress.accept(chunks.get(chunks.size() - 1));
    }

    @Override
    protected void done() {
        onDone.run();
    }
}
