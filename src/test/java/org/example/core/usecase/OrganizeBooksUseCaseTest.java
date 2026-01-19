package org.example.core.usecase;

import org.example.core.entity.Book;
import org.example.core.usecase.impl.OrganizeBooksUseCaseImpl;
import org.example.core.usecase.port.FileGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrganizeBooksUseCaseTest {

    private FileGateway fileGateway;
    private OrganizeBooksUseCase useCase;

    @BeforeEach
    void setUp() {
        fileGateway = Mockito.mock(FileGateway.class);
        useCase = new OrganizeBooksUseCaseImpl(fileGateway);
    }

    @Test
    void shouldOrganizeBook() throws IOException {
        Book book = Book.builder()
                .title("Title")
                .author("Author")
                .language("en")
                .genre("Sci-Fi")
                .series("No Series")
                .filePath(Paths.get("source.epub"))
                .build();
        Path baseDir = Paths.get("target");

        when(fileGateway.getFileSize(any())).thenReturn(100L);
        when(fileGateway.getFreeSpace(any())).thenReturn(1000L);

        useCase.execute(book, baseDir);

        verify(fileGateway).createDirectories(any());
        verify(fileGateway).copyFile(eq(book.getFilePath()), any());
    }

    @Test
    void shouldThrowExceptionWhenNotEnoughSpace() throws IOException {
        Book book = Book.builder()
                .title("Title")
                .filePath(Paths.get("source.epub"))
                .build();
        Path baseDir = Paths.get("target");

        when(fileGateway.getFileSize(any())).thenReturn(1000L);
        when(fileGateway.getFreeSpace(any())).thenReturn(100L);

        assertThrows(IOException.class, () -> useCase.execute(book, baseDir));
    }
}
