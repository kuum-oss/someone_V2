package org.example.core.usecase.impl;

import org.example.core.entity.Book;
import org.example.core.usecase.OrganizeBooksUseCase;
import org.example.core.usecase.port.FileGateway;
import org.example.core.util.TextNormalizer;

import java.io.IOException;
import java.nio.file.Path;

public class OrganizeBooksUseCaseImpl implements OrganizeBooksUseCase {

    private final FileGateway fileGateway;

    public OrganizeBooksUseCaseImpl(FileGateway fileGateway) {
        this.fileGateway = fileGateway;
    }

    @Override
    public void execute(Book book, Path baseDir) throws IOException {
        Path root = getCollectionRoot(baseDir);

        Path target = root
                .resolve(TextNormalizer.toSafeFileName(TextNormalizer.normalizeLanguage(book.getLanguage())))
                .resolve(TextNormalizer.toSafeFileName(book.getGenre()));

        if (!"No Series".equalsIgnoreCase(book.getSeries())) {
            target = target.resolve(TextNormalizer.toSafeFileName(book.getSeries()));
        }

        fileGateway.createDirectories(target);

        Path sourcePath = book.getFilePath();
        Path targetPath = target.resolve(sourcePath.getFileName());

        checkFreeSpace(sourcePath, target, baseDir);

        fileGateway.copyFile(sourcePath, targetPath);
    }

    private Path getCollectionRoot(Path baseDir) {
        return baseDir.getFileName() != null &&
                baseDir.getFileName().toString().equalsIgnoreCase("collection")
                ? baseDir
                : baseDir.resolve("collection");
    }

    private void checkFreeSpace(Path sourcePath, Path target, Path baseDir) throws IOException {
        long fileSize = fileGateway.getFileSize(sourcePath);
        long freeSpace = fileGateway.getFreeSpace(target.getRoot() != null ? target.getRoot() : baseDir);

        if (freeSpace < fileSize) {
            throw new IOException("Not enough free space on disk. Required: " + fileSize + ", Available: " + freeSpace);
        }
    }
}
