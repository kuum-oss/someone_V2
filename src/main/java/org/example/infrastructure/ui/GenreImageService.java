package org.example.infrastructure.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenreImageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenreImageService.class);
    private final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();

    private static final Map<String, String> GENRE_ICONS = new HashMap<>();

    static {
        GENRE_ICONS.put("Science Fiction", "scifi.png");
        GENRE_ICONS.put("Sci-Fi", "scifi.png");
        GENRE_ICONS.put("Fantasy", "fantasy.png");
        GENRE_ICONS.put("Horror", "horror.png");
        GENRE_ICONS.put("Thriller", "thriller.png");
        GENRE_ICONS.put("Suspense", "thriller.png");
        GENRE_ICONS.put("Romance", "romance.png");
        GENRE_ICONS.put("Detective", "detective.png");
        GENRE_ICONS.put("Mystery", "mystery.png");
        GENRE_ICONS.put("History", "history.png");
        GENRE_ICONS.put("Biography", "history.png");
        GENRE_ICONS.put("Programming", "programming.png");
        GENRE_ICONS.put("Computers", "computers.png");
        GENRE_ICONS.put("Software", "computers.png");
        GENRE_ICONS.put("Psychology", "psychology.png");
        GENRE_ICONS.put("Philosophy", "psychology.png");
        GENRE_ICONS.put("Fiction", "book.png");
        GENRE_ICONS.put("Classic", "book.png");
        GENRE_ICONS.put("Action", "action.png");
        GENRE_ICONS.put("Adventure", "adventure.png");
        GENRE_ICONS.put("Dystopia", "scifi.png");
        GENRE_ICONS.put("Crime", "detective.png");
        GENRE_ICONS.put("Artists", "history.png");
        GENRE_ICONS.put("Art", "history.png");
        GENRE_ICONS.put("Business", "computers.png");
        GENRE_ICONS.put("Economics", "computers.png");
        GENRE_ICONS.put("Science", "scifi.png");
        GENRE_ICONS.put("Cooking", "book.png");
        GENRE_ICONS.put("Children", "adventure.png");
        GENRE_ICONS.put("Juvenile", "adventure.png");
        GENRE_ICONS.put("Philosophy", "psychology.png");
        GENRE_ICONS.put("Religion", "history.png");
        GENRE_ICONS.put("Travel", "adventure.png");
        GENRE_ICONS.put("General", "book.png");
        GENRE_ICONS.put("Unknown", "book.png");
        
        // Russian mappings for FB2
        GENRE_ICONS.put("Фантастика", "scifi.png");
        GENRE_ICONS.put("Фэнтези", "fantasy.png");
        GENRE_ICONS.put("Ужасы", "horror.png");
        GENRE_ICONS.put("Триллер", "thriller.png");
        GENRE_ICONS.put("Детектив", "detective.png");
        GENRE_ICONS.put("Роман", "romance.png");
        GENRE_ICONS.put("Приключения", "adventure.png");
        GENRE_ICONS.put("История", "history.png");
        GENRE_ICONS.put("Наука", "scifi.png");
        GENRE_ICONS.put("Психология", "psychology.png");
        GENRE_ICONS.put("Программирование", "programming.png");
        GENRE_ICONS.put("Компьютеры", "computers.png");
    }

    private static final Map<String, String> REMOTE_FALLBACKS = new HashMap<>();

    static {
        REMOTE_FALLBACKS.put("Science Fiction", "https://cdn-icons-png.flaticon.com/512/6119/6119533.png");
        REMOTE_FALLBACKS.put("Fantasy", "https://cdn-icons-png.flaticon.com/512/1065/1065051.png");
        REMOTE_FALLBACKS.put("Horror", "https://cdn-icons-png.flaticon.com/512/1065/1065056.png");
        REMOTE_FALLBACKS.put("Romance", "https://cdn-icons-png.flaticon.com/512/833/833472.png");
        REMOTE_FALLBACKS.put("Detective", "https://cdn-icons-png.flaticon.com/512/3504/3504445.png");
        REMOTE_FALLBACKS.put("History", "https://cdn-icons-png.flaticon.com/512/2618/2618239.png");
        REMOTE_FALLBACKS.put("Programming", "https://cdn-icons-png.flaticon.com/512/1149/1149168.png");
        REMOTE_FALLBACKS.put("Computers", "https://cdn-icons-png.flaticon.com/512/3062/3062310.png");
        REMOTE_FALLBACKS.put("Fiction", "https://cdn-icons-png.flaticon.com/512/3389/3389032.png");
        REMOTE_FALLBACKS.put("Biography", "https://cdn-icons-png.flaticon.com/512/2618/2618239.png");
        REMOTE_FALLBACKS.put("Business", "https://cdn-icons-png.flaticon.com/512/1508/1508880.png");
        REMOTE_FALLBACKS.put("Science", "https://cdn-icons-png.flaticon.com/512/1048/1048953.png");
    }

    public ImageIcon getDefaultBookIcon() {
        return getIconFromResource("book.png");
    }

    public ImageIcon getGenreIcon(String genre) {
        if (genre == null || GraphicsEnvironment.isHeadless()) return null;

        return cache.computeIfAbsent(genre, g -> {
            String iconName = findInMap(g, GENRE_ICONS);
            if (iconName != null) {
                ImageIcon icon = getIconFromResource(iconName);
                if (icon != null) return icon;
            }

            String remoteUrl = findInMap(g, REMOTE_FALLBACKS);
            if (remoteUrl != null) {
                ImageIcon icon = downloadIcon(remoteUrl);
                if (icon != null) return icon;
            }

            return createPixelIcon(g);
        });
    }

    private String findInMap(String genre, Map<String, String> map) {
        String lower = genre.toLowerCase();
        return map.entrySet().stream()
                .filter(e -> lower.contains(e.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private ImageIcon getIconFromResource(String iconName) {
        try {
            URL url = getClass().getResource("/icons/" + iconName);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                if (icon.getImage() != null) {
                    return scale(icon, 24, 24);
                }
            } else {
                LOGGER.warn("Icon not found: /icons/{}", iconName);
            }
        } catch (Exception e) {
            LOGGER.error("Resource icon error: {}", iconName, e);
        }
        return null;
    }

    private ImageIcon downloadIcon(String url) {
        try {
            ImageIcon icon = new ImageIcon(new URL(url));
            if (icon.getImage() != null) {
                return scale(icon, 24, 24);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to download icon from {}: {}", url, e.getMessage());
        }
        return null;
    }

    private ImageIcon scale(ImageIcon icon, int w, int h) {
        if (icon != null && icon.getImage() != null && icon.getIconWidth() > 0) {
            return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
        }
        return null;
    }

    private ImageIcon createPixelIcon(String genre) {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Детерминированный цвет на основе названия жанра
        int hash = genre.hashCode();
        Color baseColor = new Color(
                (hash & 0xFF0000) >> 16,
                (hash & 0x00FF00) >> 8,
                (hash & 0x0000FF)
        );

        g2.setColor(baseColor);
        // Рисуем "пиксельный" паттерн
        int pixelSize = 4;
        for (int x = 0; x < size; x += pixelSize) {
            for (int y = 0; y < size; y += pixelSize) {
                if (((hash >> (x + y)) & 1) == 1) {
                    g2.fillRect(x, y, pixelSize, pixelSize);
                }
            }
        }

        // Добавляем рамку
        g2.setColor(baseColor.darker());
        g2.drawRect(0, 0, size - 1, size - 1);

        g2.dispose();
        return new ImageIcon(img);
    }
}
