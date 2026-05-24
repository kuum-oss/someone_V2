package org.example;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TikaDependencyTest {

    @Test
    public void testTikaWithZip() throws Exception {
        // Создаем минимальный ZIP-архив (EPUB по сути является ZIP)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("mimetype");
            zos.putNextEntry(entry);
            zos.write("application/epub+zip".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        byte[] zipContent = baos.toByteArray();

        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (InputStream is = new ByteArrayInputStream(zipContent)) {
            // Если есть конфликт commons-compress, это выбросит NoSuchMethodError
            try {
                parser.parse(is, handler, metadata, context);
            } catch (NoSuchMethodError e) {
                throw new org.opentest4j.AssertionFailedError("Caught NoSuchMethodError: " + e.getMessage(), e);
            } catch (Exception e) {
                // Другие ошибки парсинга (например, невалидный ZIP) нас не интересуют
                System.out.println("Caught expected or irrelevant exception: " + e.getClass().getName());
            }
        }
    }
}
