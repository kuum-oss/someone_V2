package org.example.core.usecase;

import org.example.core.entity.Book;
import java.nio.file.Path;

public interface ExtractMetadataUseCase {
    Book execute(Path path);
}
