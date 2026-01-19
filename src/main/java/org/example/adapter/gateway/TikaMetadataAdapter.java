package org.example.adapter.gateway;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.example.core.usecase.port.MetadataGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TikaMetadataAdapter implements MetadataGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(TikaMetadataAdapter.class);
    private final Parser parser = new AutoDetectParser();

    @Override
    public Map<String, String> extractRawMetadata(Path path) {
        Metadata md = new Metadata();
        try (InputStream in = Files.newInputStream(path)) {
            parser.parse(in, new BodyContentHandler(-1), md, new ParseContext());
        } catch (Exception e) {
            LOGGER.error("Error parsing metadata for file: {}", path, e);
        }

        Map<String, String> result = new HashMap<>();
        for (String name : md.names()) {
            result.put(name, md.get(name));
        }
        return result;
    }

    @Override
    public byte[] extractCover(Path path) {
        // Tika often puts cover in metadata if it's FB2
        Metadata md = new Metadata();
        try (InputStream in = Files.newInputStream(path)) {
            parser.parse(in, new BodyContentHandler(-1), md, new ParseContext());
            String base64 = md.get("fb2:cover");
            if (base64 != null && !base64.isBlank()) {
                return Base64.getDecoder().decode(base64.replaceAll("\\s", ""));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to extract cover from {}", path);
        }
        return null;
    }
}
