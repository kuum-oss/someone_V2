package org.example.core.usecase;

import org.example.core.entity.Book;
import java.io.IOException;
import java.nio.file.Path;
// Use case для организации (размещения) книги в файловой структуре библиотеки

public interface OrganizeBooksUseCase {
    void execute(Book book, Path baseDir) throws IOException;
}
