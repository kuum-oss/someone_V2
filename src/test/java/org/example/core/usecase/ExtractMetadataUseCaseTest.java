package org.example.core.usecase;

import org.example.core.entity.Book;
import org.example.core.usecase.impl.ExtractMetadataUseCaseImpl;
import org.example.core.usecase.port.ExternalMetadataGateway;
import org.example.core.usecase.port.MetadataGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ExtractMetadataUseCaseTest {

    private MetadataGateway metadataGateway;
    private ExternalMetadataGateway externalMetadataGateway;
    private ExtractMetadataUseCase useCase;

    @BeforeEach
    void setUp() {
        metadataGateway = Mockito.mock(MetadataGateway.class);
        externalMetadataGateway = Mockito.mock(ExternalMetadataGateway.class);
        useCase = new ExtractMetadataUseCaseImpl(metadataGateway, externalMetadataGateway);
    }

    @Test
    void shouldExtractMetadataFromLocalGateway() {
        Path path = Paths.get("test.fb2");
        Map<String, String> rawMd = new HashMap<>();
        rawMd.put("dc:title", "Test Title");
        rawMd.put("dc:creator", "Test Author");
        rawMd.put("dc:language", "en-US");

        when(metadataGateway.extractRawMetadata(path)).thenReturn(rawMd);
        when(metadataGateway.extractCover(path)).thenReturn(new byte[]{1, 2, 3});

        Book book = useCase.execute(path);

        assertEquals("Test Title", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
        assertEquals("en", book.getLanguage());
        assertArrayEquals(new byte[]{1, 2, 3}, book.getCover());
    }

    @Test
    void shouldFallbackToExternalGatewayForMissingGenre() {
        Path path = Paths.get("test.fb2");
        Map<String, String> rawMd = new HashMap<>();
        rawMd.put("dc:title", "Test Title");
        rawMd.put("dc:creator", "Test Author");

        when(metadataGateway.extractRawMetadata(path)).thenReturn(rawMd);
        when(externalMetadataGateway.fetchGenre("Test Title", "Test Author")).thenReturn(Optional.of("Fantasy"));

        Book book = useCase.execute(path);

        assertEquals("Fantasy", book.getGenre());
    }
    
}
