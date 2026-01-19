package org.example.core.usecase.port;

import org.example.core.entity.Book;
import java.io.IOException;
import java.nio.file.Path;

public interface FileGateway {
    long getFreeSpace(Path path) throws IOException;
    long getFileSize(Path path) throws IOException;
    void copyFile(Path source, Path target) throws IOException;
    void createDirectories(Path path) throws IOException;
}
