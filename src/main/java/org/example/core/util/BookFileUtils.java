package org.example.core.util;

import java.nio.file.Path;
// Вспомогательный класс для определения поддерживаемых файлов электронных книг

public class BookFileUtils {
    public static boolean isBookFile(Path path) {
        if (path == null) return false;
        String n = path.getFileName().toString().toLowerCase();
        return n.endsWith(".pdf") || n.endsWith(".epub") || n.endsWith(".fb2") || n.endsWith(".mobi");
    }
    
    public static boolean isBookFile(String fileName) {
        if (fileName == null) return false;
        String n = fileName.toLowerCase();
        return n.endsWith(".pdf") || n.endsWith(".epub") || n.endsWith(".fb2") || n.endsWith(".mobi");
    }
}
