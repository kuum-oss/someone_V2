package org.example.core.usecase;

import org.example.core.entity.Book;
import java.io.IOException;
import java.nio.file.Path;

public interface OrganizeBooksUseCase {
    void execute(Book book, Path baseDir) throws IOException;
}
