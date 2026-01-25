package org.example.core.usecase;

import org.example.core.entity.Book;
import java.nio.file.Path;
// Use case для извлечения метаданных книги из файла по указанному пути
public interface ExtractMetadataUseCase {
    Book execute(Path path);
}
