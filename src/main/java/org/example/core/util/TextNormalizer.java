package org.example.core.util;

import java.util.Set;

public class TextNormalizer {

    private static final Set<String> SMALL_WORDS = Set.of(
            "a", "an", "the", "and", "or", "in", "on", "at", "to", "for", "of", "with", 
            "и", "или", "в", "на", "с"
    );

    public static String normalizeLanguage(String lang) {
        if (lang == null || lang.isBlank()) return "Unknown";
        String l = lang.toLowerCase().trim();
        if (l.contains("-")) {
            l = l.split("-")[0];
        } else if (l.contains("_")) {
            l = l.split("_")[0];
        }
        return l;
    }

    public static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) return title;
        
        String work = title.trim();

        // 1. Detect and strip paths (Windows or Unix)
        if (work.contains("\\") || work.contains("/")) {
            // If it looks like a path, take the last segment
            // Patterns: D:\path\file, /path/file, C:/path/file
            String[] segments = work.split("[\\\\/]");
            if (segments.length > 1) {
                work = segments[segments.length - 1];
            }
        }

        // 2. Remove common format suffixes if they are separated by space or dot (e.g., "book Epub", "book.pdf")
        // and other conversion garbage
        work = work.replaceAll("(?i)[\\s._-]+(epub|pdf|fb2|mobi)$", "");
        
        // 3. Remove leading/trailing non-alphanumeric characters (like slashes or dots)
        work = work.replaceAll("^[^\\p{L}\\p{N}]+", "");
        work = work.replaceAll("[^\\p{L}\\p{N}]+$", "");

        // 4. Try to transliterate if it looks like Russian/Ukrainian translit
        if (TransliterationUtils.isProbablyTranslit(work)) {
            work = TransliterationUtils.transliterate(work);
        }

        String normalized = work.replaceAll("[-_.]", " ");
        String[] words = normalized.split("\\s+");
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (!word.isEmpty()) {
                if (i > 0 && SMALL_WORDS.contains(word) && i < words.length - 1) {
                    sb.append(word);
                } else {
                    sb.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1));
                }
                sb.append(" ");
            }
        }
        String result = sb.toString().trim();
        return result.isEmpty() ? "Unknown" : result;
    }

    public static String toSafeFileName(String s) {
        return (s == null || s.isBlank()) ? "Unknown"
                : s.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static String normalizeGenre(String rawGenre) {
        if (rawGenre == null || rawGenre.isBlank()) return "General";
        
        String lower = rawGenre.toLowerCase();
        
        // Search in the whole string first
        if (lower.contains("prog") || lower.contains("software")) return "Programming";
        if (lower.contains("comp")) return "Computers";
        if (lower.contains("sci") && lower.contains("fic")) return "Science Fiction";
        if (lower.contains("fant")) return "Fantasy";
        if (lower.contains("myst") || lower.contains("susp") || lower.contains("thrill")) return "Thriller";
        if (lower.contains("detect") || lower.contains("crime")) return "Detective";
        if (lower.contains("romance") || lower.contains("love")) return "Romance";
        if (lower.contains("hist")) return "History";
        if (lower.contains("bio") || lower.contains("memo")) return "Biography";
        if (lower.contains("psych")) return "Psychology";
        if (lower.contains("art")) return "Art";
        if (lower.contains("bus") || lower.contains("econ")) return "Business";
        if (lower.contains("cook")) return "Cooking";
        if (lower.contains("juv") || lower.contains("child")) return "Children";
        if (lower.contains("phil")) return "Philosophy";
        if (lower.contains("relig")) return "Religion";
        if (lower.contains("travel")) return "Travel";
        
        // Google Books often returns genres like "Fiction / Science Fiction"
        String g = rawGenre.contains("/") ? rawGenre.substring(rawGenre.lastIndexOf("/") + 1).trim() : rawGenre.trim();
        
        // For FB2 genres which are often codes or Russian
        if (lower.contains("fan") || lower.contains("fic")) {
             if (lower.contains("sci")) return "Science Fiction";
             return "Fiction";
        }

        return normalizeTitle(g); // Default to title-cased normalized string
    }
}
