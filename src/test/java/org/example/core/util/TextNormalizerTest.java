package org.example.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextNormalizerTest {

    @Test
    void shouldNormalizeSimpleTitle() {
        assertEquals("The Great Gatsby", TextNormalizer.normalizeTitle("the great gatsby"));
    }

    @Test
    void shouldHandleUnderscoresAndDashes() {
        assertEquals("My Book Title", TextNormalizer.normalizeTitle("my_book-title"));
    }

    @Test
    void shouldHandleLeadingSlash() {
        // Тест на случай "/bezserdechna."
        // После stripExtension будет "/bezserdechna"
        // Ожидаем "Безсердечна" (после транслитерации) или хотя бы "Bezserdechna" без слэша
        String result = TextNormalizer.normalizeTitle("/bezserdechna");
        assertFalse(result.startsWith("/"), "Title should not start with a slash");
    }

    @Test
    void shouldHandleFullUserExample() {
        String input = "Choffi_Djessika_Devushka_so_snejnyim_serdtsem";
        String result = TextNormalizer.normalizeTitle(input);
        // "Choffi_Djessika_Devushka_so_snejnyim_serdtsem" 
        // -> contains '_', contains 'sh' -> isProbablyTranslit = true
        // -> transliterated
        // -> underscores to spaces
        // -> capitalized
        assertTrue(result.contains("Девушка"), "Should contain 'Девушка', but was: " + result);
        assertTrue(result.contains("Сердтсем") || result.contains("Сердцем"), "Should contain 'Сердцем', but was: " + result);
    }

    @Test
    void shouldCleanPathLikeTitles() {
        // BUG-001 reproduction
        String input = "D:\\wwwroot\\cleverpdf Web\\10750567\\ubivstvoetiket Epub";
        String result = TextNormalizer.normalizeTitle(input);
        
        // Expected: "Убивство етикет" (after translit and cleaning) or at least "Ubivstvoetiket"
        assertTrue(result.contains("Убивство") || result.contains("Ubivstvo"), 
            "Title should be cleaned from path, but was: " + result);
        assertFalse(result.contains("wwwroot"), "Title should not contain path elements");
        assertFalse(result.contains("\\"), "Title should not contain backslashes");
    }

    @Test
    void shouldNormalizeGenres() {
        assertEquals("Science Fiction", TextNormalizer.normalizeGenre("Fiction / Science Fiction"));
        assertEquals("Programming", TextNormalizer.normalizeGenre("Computers / Programming / General"));
        assertEquals("Thriller", TextNormalizer.normalizeGenre("Fiction / Suspense"));
        assertEquals("General", TextNormalizer.normalizeGenre(null));
        assertEquals("Fantasy", TextNormalizer.normalizeGenre("fantasy"));
    }
}
