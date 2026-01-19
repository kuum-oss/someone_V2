package org.example.adapter.gateway;

import org.example.core.entity.Book;
import org.example.core.usecase.port.FileGateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NioFileAdapter implements FileGateway {
    @Override
    public long getFreeSpace(Path path) throws IOException {
        return Files.getFileStore(path).getUsableSpace();
    }

    @Override
    public long getFileSize(Path path) throws IOException {
        return Files.size(path);
    }

    @Override
    public void copyFile(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }
}
