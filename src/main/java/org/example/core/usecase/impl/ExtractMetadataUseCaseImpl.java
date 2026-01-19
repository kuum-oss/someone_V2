package org.example.core.usecase.impl;

import org.example.core.entity.Book;
import org.example.core.usecase.ExtractMetadataUseCase;
import org.example.core.usecase.port.ExternalMetadataGateway;
import org.example.core.usecase.port.MetadataGateway;
import org.example.core.util.TextNormalizer;

import java.nio.file.Path;
import java.util.Map;

public class ExtractMetadataUseCaseImpl implements ExtractMetadataUseCase {

    private final MetadataGateway metadataGateway;
    private final ExternalMetadataGateway externalMetadataGateway;

    public ExtractMetadataUseCaseImpl(MetadataGateway metadataGateway, ExternalMetadataGateway externalMetadataGateway) {
        this.metadataGateway = metadataGateway;
        this.externalMetadataGateway = externalMetadataGateway;
    }

    @Override
    public Book execute(Path path) {
        Map<String, String> md = metadataGateway.extractRawMetadata(path);

        String title = findFirst(md, "dc:title", "title", "cp:title");
        if (title == null || title.isBlank() || isGenericUnknown(title)) {
            title = stripExtension(path.getFileName().toString());
        }
        title = TextNormalizer.normalizeTitle(title);

        String author = findFirst(md, "dc:creator", "creator", "author", "meta:author");
        if (author == null || author.isBlank() || isGenericUnknown(author)) {
            author = "Unknown";
        }

        String language = TextNormalizer.normalizeLanguage(defaultIfBlank(md.get("dc:language"), "Unknown"));
        String series = defaultIfBlank(md.get("fb2:series-name"), "No Series");
        String genre = md.get("fb2:genre");
        String year = normalizeYear(findFirst(md, "dc:date", "fb2:date", "dcterms:created", "Creation-Date"));
        String description = findFirst(md, "dc:description", "fb2:annotation", "description");

        if (genre == null || year.equals("Unknown") || description == null) {
            if (genre == null) {
                genre = externalMetadataGateway.fetchGenre(title, author).orElse(null);
            }
            if (year.equals("Unknown")) {
                year = normalizeYear(externalMetadataGateway.fetchYear(title, author).orElse("Unknown"));
            }
            if (description == null) {
                description = externalMetadataGateway.fetchDescription(title, author).orElse("");
            }
        }

        genre = TextNormalizer.normalizeGenre(genre);

        byte[] cover = metadataGateway.extractCover(path);
        if (cover == null) {
            cover = externalMetadataGateway.fetchCover(title, author).orElse(null);
        }
        byte[] authorPhoto = externalMetadataGateway.fetchAuthorPhoto(author).orElse(null);

        return Book.builder()
                .title(title)
                .author(author)
                .language(language)
                .series(series)
                .genre(genre)
                .year(year)
                .description(description)
                .filePath(path)
                .format(ext(path))
                .cover(cover)
                .authorPhoto(authorPhoto)
                .build();
    }

    private String stripExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? fileName : fileName.substring(0, lastDot);
    }

    private String findFirst(Map<String, String> md, String... keys) {
        for (String k : keys) {
            String v = md.get(k);
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private String defaultIfBlank(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private boolean isGenericUnknown(String s) {
        if (s == null) return true;
        String lower = s.toLowerCase().trim();
        return lower.equals("unknown") || lower.equals("unknown title") ||
                lower.equals("unknown author") ||
                lower.equals("untitled") || lower.equals("неизвестно") ||
                lower.equals("без названия") || lower.isEmpty();
    }

    private String normalizeYear(String year) {
        if (year == null || year.isBlank() || year.equalsIgnoreCase("Unknown")) return "Unknown";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(\\d{4})\\b").matcher(year);
        if (m.find()) {
            return m.group(1);
        }
        return year;
    }

    private String ext(Path p) {
        String n = p.getFileName().toString();
        int i = n.lastIndexOf('.');
        return i == -1 ? "" : n.substring(i + 1);
    }
}
