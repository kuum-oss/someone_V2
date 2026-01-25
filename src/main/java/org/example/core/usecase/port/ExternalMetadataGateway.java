package org.example.core.usecase.port;
// Порт для получения внешних метаданных книги из сторонних источников

import java.util.Optional;

public interface ExternalMetadataGateway {
    Optional<String> fetchGenre(String title, String author);
    Optional<String> fetchYear(String title, String author);
    Optional<String> fetchDescription(String title, String author);
    Optional<byte[]> fetchCover(String title, String author);
    Optional<byte[]> fetchAuthorPhoto(String author);
}
