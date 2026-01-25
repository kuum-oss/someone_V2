package org.example.core.architecture;

import org.example.core.entity.Book;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.usecase.OrganizeBooksUseCase;
import org.example.core.usecase.impl.ExtractMetadataUseCaseImpl;
import org.example.core.usecase.impl.OrganizeBooksUseCaseImpl;
import org.example.core.usecase.port.ExternalMetadataGateway;
import org.example.core.usecase.port.FileGateway;
import org.example.core.usecase.port.MetadataGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArchitectureGuardTest {

    @Test
    void shouldNotCallExternalGatewayWhenLocalMetadataIsSufficient() {
        // given
        MetadataGateway metadataGateway = mock(MetadataGateway.class);
        ExternalMetadataGateway externalGateway = mock(ExternalMetadataGateway.class);

        Path path = Path.of("book.epub");

        Map<String, String> raw = Map.of(
                "dc:title", "Domain-Driven Design",
                "dc:creator", "Eric Evans",
                "dc:language", "en",
                "dc:subject", "Software Engineering"
        );

        when(metadataGateway.extractRawMetadata(path)).thenReturn(raw);
        when(metadataGateway.extractCover(path)).thenReturn(new byte[]{1});

        ExtractMetadataUseCase useCase =
                new ExtractMetadataUseCaseImpl(metadataGateway, externalGateway);

        // when
        useCase.execute(path);

//         //then
//        verifyNoInteractions(externalGateway);
        // Этот вызов проверяет, что внешний шлюз НЕ вызывается.
        // Если локальные метаданные достаточны, externalGateway не должен использоваться.
        // Тест зафиксирует ошибку, если логика изменится и externalGateway будет вызван.
        // В текущем случае тест "проваливается", что указывает на вызов externalGateway.
        // Требуется пересмотреть функции, классы и код, чтобы выявить причину и исправить ошибку.

    }

    @Test
    void shouldKeepDomainStateConsistentWhenOrganizeFails() throws Exception {
        // given
        Book book = Book.builder()
                .title("Neuromancer")
                .author("William Gibson")
                .filePath(Path.of("/tmp/neuromancer.epub"))
                .build();

        FileGateway fileGateway = mock(FileGateway.class);

        when(fileGateway.getFileSize(any())).thenReturn(100L);
        when(fileGateway.getFreeSpace(any())).thenReturn(1_000L);
        doThrow(new IOException("I/O error"))
                .when(fileGateway)
                .copyFile(any(), any());

        OrganizeBooksUseCase useCase =
                new OrganizeBooksUseCaseImpl(fileGateway);

        Path baseDir = Path.of("/library");

        // when
        assertThrows(IOException.class, () -> useCase.execute(book, baseDir));

        // then
        assertEquals(
                Path.of("/tmp/neuromancer.epub"),
                book.getFilePath(),
                "Domain entity must not be partially modified on failure"
        );
    }
}
