package net.lax1dude.eaglercraft.mod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Reads the portable .fmod.zip package format. */
public final class ModPackageReader {

    private static final int MAX_METADATA_BYTES = 64 * 1024;
    private static final int MAX_PAYLOAD_BYTES = 16 * 1024 * 1024;
    private static final int MAX_TOTAL_BYTES = 128 * 1024 * 1024;

    private ModPackageReader() {
    }

    public static ModPackage read(InputStream input) throws IOException {
        if (input == null) {
            throw new IOException("Missing mod package input");
        }
        byte[] metadata = null;
        Map<String, byte[]> payloads = new LinkedHashMap<>();
        int totalBytes = 0;

        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = normalizeEntryName(entry.getName());
                if ("mod.fbt".equals(name) || name.endsWith(".fbt")) {
                    if (metadata != null) {
                        throw new IOException("A package may contain only one .fbt metadata file");
                    }
                    metadata = readEntry(zip, MAX_METADATA_BYTES);
                    totalBytes += metadata.length;
                    if (totalBytes > MAX_TOTAL_BYTES) {
                        throw new IOException("Mod package exceeds " + MAX_TOTAL_BYTES + " bytes");
                    }
                } else if (name.endsWith(".fbm")) {
                    if (payloads.containsKey(name)) {
                        throw new IOException("Duplicate payload: " + name);
                    }
                    byte[] payload = readEntry(zip, MAX_PAYLOAD_BYTES);
                    totalBytes += payload.length;
                    if (totalBytes > MAX_TOTAL_BYTES) {
                        throw new IOException("Mod package exceeds " + MAX_TOTAL_BYTES + " bytes");
                    }
                    payloads.put(name, payload);
                }
                zip.closeEntry();
            }
        }

        if (metadata == null) {
            throw new IOException("Missing .fbt metadata file");
        }
        if (payloads.isEmpty()) {
            throw new IOException("Package contains no .fbm payloads");
        }
        return createPackage(metadata, payloads);
    }

    private static ModPackage createPackage(byte[] metadata, Map<String, byte[]> payloads) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = new ByteArrayInputStream(metadata)) {
            properties.load(input);
        }
        String id = required(properties, "id");
        String title = required(properties, "title", "name");
        String description = properties.getProperty("description", "");
        String version = properties.getProperty("version", "1.0.0");
        String source = properties.getProperty("source", "unknown");
        String format = properties.getProperty("format", "unknown");
        if (!id.matches("[a-z0-9][a-z0-9._-]{0,63}")) {
            throw new IOException("Invalid mod id: " + id);
        }
        return new ModPackage(id, title, description, version, source, format, payloads);
    }

    private static String required(Properties properties, String... keys) throws IOException {
        for (String key : keys) {
            String value = properties.getProperty(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        throw new IOException("Missing metadata property: " + keys[0]);
    }

    private static String normalizeEntryName(String name) throws IOException {
        if (name == null || name.isEmpty() || name.startsWith("/") || name.indexOf('\\') >= 0) {
            throw new IOException("Invalid ZIP entry name");
        }
        String[] parts = name.split("/");
        for (String part : parts) {
            if (part.isEmpty() || ".".equals(part) || "..".equals(part)) {
                throw new IOException("Unsafe ZIP entry name: " + name);
            }
        }
        return name;
    }

    private static byte[] readEntry(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (output.size() > limit - count) {
                throw new IOException("ZIP entry exceeds " + limit + " bytes");
            }
            output.write(buffer, 0, count);
        }
        return output.toByteArray();
    }
}
