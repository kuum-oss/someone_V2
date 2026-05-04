package org.example.core.usecase.port;

import org.example.core.entity.Book;
import java.nio.file.Path;
import java.util.Map;
// Порт для доступа к источникам метаданных книги (файл, парсер, библиотека)

public interface MetadataGateway {
    Map<String, String> extractRawMetadata(Path path);
    byte[] extractCover(Path path);
    String extractTextPreview(byte[] content, int maxChars);
    String extractFullText(byte[] content);
}
