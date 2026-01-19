package org.example.core.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransliterationUtils {

    private static final Map<String, String> LAT_TO_CYR = new LinkedHashMap<>();

    static {
        // Multi-character sequences first
        LAT_TO_CYR.put("shch", "щ");
        LAT_TO_CYR.put("sh", "ш");
        LAT_TO_CYR.put("ch", "ч");
        LAT_TO_CYR.put("zh", "ж");
        LAT_TO_CYR.put("kh", "х");
        LAT_TO_CYR.put("ts", "ц");
        LAT_TO_CYR.put("yu", "ю");
        LAT_TO_CYR.put("ya", "я");
        LAT_TO_CYR.put("yo", "ё");
        LAT_TO_CYR.put("dj", "дж");
        LAT_TO_CYR.put("ij", "ий");
        LAT_TO_CYR.put("iy", "ий");
        LAT_TO_CYR.put("yi", "ый");
        LAT_TO_CYR.put("yy", "ый");
        
        // Single characters
        LAT_TO_CYR.put("a", "а");
        LAT_TO_CYR.put("b", "б");
        LAT_TO_CYR.put("v", "в");
        LAT_TO_CYR.put("g", "г");
        LAT_TO_CYR.put("d", "д");
        LAT_TO_CYR.put("e", "е");
        LAT_TO_CYR.put("z", "з");
        LAT_TO_CYR.put("i", "и");
        LAT_TO_CYR.put("j", "й");
        LAT_TO_CYR.put("k", "к");
        LAT_TO_CYR.put("l", "л");
        LAT_TO_CYR.put("m", "м");
        LAT_TO_CYR.put("n", "н");
        LAT_TO_CYR.put("o", "о");
        LAT_TO_CYR.put("p", "п");
        LAT_TO_CYR.put("r", "р");
        LAT_TO_CYR.put("s", "с");
        LAT_TO_CYR.put("t", "т");
        LAT_TO_CYR.put("u", "у");
        LAT_TO_CYR.put("f", "ф");
        LAT_TO_CYR.put("h", "х");
        LAT_TO_CYR.put("y", "ы");
        LAT_TO_CYR.put("'", "ь");
    }

    public static String transliterate(String text) {
        if (text == null || text.isBlank()) return text;

        String result = text;
        // Handle case sensitivity roughly by checking first letter
        for (Map.Entry<String, String> entry : LAT_TO_CYR.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Replacement for lower case
            result = result.replace(key, value);
            // Replacement for upper case
            result = result.replace(key.substring(0, 1).toUpperCase() + key.substring(1), 
                                    value.toUpperCase());
            if (key.length() > 1) {
                 result = result.replace(key.toUpperCase(), value.toUpperCase());
            }
        }
        return result;
    }

    public static boolean isProbablyTranslit(String text) {
        if (text == null || text.isBlank()) return false;
        
        // If it already has Cyrillic, it's not translit
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= '\u0400' && c <= '\u04FF') || (c >= '\u0500' && c <= '\u052F')) {
                return false;
            }
        }

        String lower = text.toLowerCase();
        
        // 1. Very strong markers (almost never in English or have different meaning)
        String[] veryStrong = {"shch", "zh", "iy", "yi", "ij", "yy", "jje", "aye", "uye"};
        for (String m : veryStrong) {
            if (lower.contains(m)) return true;
        }

        // 2. If it's a file-name style (with underscores)
        if (lower.contains("_")) {
            // Check for RU/UKR phonemes
            String[] phonemes = {"kh", "ts", "yu", "ya", "yo", "sh", "ch"};
            for (String p : phonemes) {
                if (lower.contains(p)) return true;
            }
        }

        // 3. If it's a single word without spaces
        if (!text.contains(" ")) {
            // More aggressive markers for single words
            if (lower.startsWith("bez") || lower.endsWith("na") || lower.endsWith("va") || lower.endsWith("ko")) {
                return true;
            }
            if (lower.contains("vst") || lower.contains("stvo") || lower.contains("dzh")) {
                return true;
            }
            if (lower.contains("ch") && (lower.contains("v") || lower.contains("z") || lower.contains("n"))) {
                return true;
            }
        }

        return false;
    }
}
