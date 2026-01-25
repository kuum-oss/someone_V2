package org.example.core.usecase.impl;

import org.example.core.entity.Book;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.usecase.port.ExternalMetadataGateway;
import org.example.core.usecase.port.MetadataGateway;
import org.example.core.util.TextNormalizer;

import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractMetadataUseCaseImpl implements ExtractMetadataUseCase {

    private static final String UNKNOWN = "Unknown";

    private final MetadataGateway metadataGateway;
    private final ExternalMetadataGateway externalMetadataGateway;

    public ExtractMetadataUseCaseImpl(
            MetadataGateway metadataGateway,
            ExternalMetadataGateway externalMetadataGateway
    ) {
        this.metadataGateway = metadataGateway;
        this.externalMetadataGateway = externalMetadataGateway;
    }

    @Override
    public Book execute(Path path) {
        Map<String, String> md = metadataGateway.extractRawMetadata(path);

        String title = resolveTitle(md, path);
        String author = resolveAuthor(md);
        String language = resolveLanguage(md);
        String series = resolveSeries(md);
        String genre = md.get("fb2:genre");
        String year = normalizeYear(findFirst(md,
                "dc:date", "fb2:date", "dcterms:created", "Creation-Date"));
        String description = findFirst(md,
                "dc:description", "fb2:annotation", "description");

        boolean localMetadataIsSufficient =
                genre != null &&
                        !UNKNOWN.equals(year) &&
                        description != null;

        if (!localMetadataIsSufficient) {
            if (genre == null) {
                genre = externalMetadataGateway.fetchGenre(title, author).orElse(null);
            }
            if (UNKNOWN.equals(year)) {
                year = normalizeYear(
                        externalMetadataGateway.fetchYear(title, author).orElse(UNKNOWN)
                );
            }
            if (description == null) {
                description = externalMetadataGateway
                        .fetchDescription(title, author)
                        .orElse("");
            }
        }

        genre = TextNormalizer.normalizeGenre(genre);

        byte[] cover = metadataGateway.extractCover(path);
        if (!localMetadataIsSufficient && cover == null) {
            cover = externalMetadataGateway.fetchCover(title, author).orElse(null);
        }

        byte[] authorPhoto = null;
        if (!localMetadataIsSufficient && !UNKNOWN.equals(author)) {
            authorPhoto = externalMetadataGateway.fetchAuthorPhoto(author).orElse(null);
        }

        return Book.builder()
                .title(title)
                .author(author)
                .language(language)
                .series(series)
                .genre(genre)
                .year(year)
                .description(description)
                .filePath(path)
                .format(extension(path))
                .cover(cover)
                .authorPhoto(authorPhoto)
                .build();
    }

    /* =========================
       Resolution helpers
       ========================= */

    private String resolveTitle(Map<String, String> md, Path path) {
        String title = findFirst(md, "dc:title", "title", "cp:title");
        if (isBlankOrUnknown(title)) {
            title = stripExtension(path.getFileName().toString());
        }
        return TextNormalizer.normalizeTitle(title);
    }

    private String resolveAuthor(Map<String, String> md) {
        String author = findFirst(md, "dc:creator", "creator", "author", "meta:author");
        return isBlankOrUnknown(author) ? UNKNOWN : author;
    }

    private String resolveLanguage(Map<String, String> md) {
        return TextNormalizer.normalizeLanguage(
                defaultIfBlank(md.get("dc:language"), UNKNOWN)
        );
    }

    private String resolveSeries(Map<String, String> md) {
        return defaultIfBlank(md.get("fb2:series-name"), "No Series");
    }

    /* =========================
       Utility methods
       ========================= */

    private String findFirst(Map<String, String> md, String... keys) {
        for (String k : keys) {
            String v = md.get(k);
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private boolean isBlankOrUnknown(String value) {
        if (value == null) return true;
        String v = value.trim().toLowerCase();
        return v.isEmpty()
                || v.equals("unknown")
                || v.equals("unknown title")
                || v.equals("unknown author")
                || v.equals("untitled")
                || v.equals("неизвестно")
                || v.equals("без названия");
    }

    private String defaultIfBlank(String value, String def) {
        return (value == null || value.isBlank()) ? def : value;
    }

    private String normalizeYear(String year) {
        if (year == null || year.isBlank() || UNKNOWN.equalsIgnoreCase(year)) {
            return UNKNOWN;
        }
        Matcher m = Pattern.compile("\\b(\\d{4})\\b").matcher(year);
        return m.find() ? m.group(1) : year;
    }

    private String stripExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i == -1 ? fileName : fileName.substring(0, i);
    }

    private String extension(Path path) {
        String n = path.getFileName().toString();
        int i = n.lastIndexOf('.');
        return i == -1 ? "" : n.substring(i + 1);
    }
}
