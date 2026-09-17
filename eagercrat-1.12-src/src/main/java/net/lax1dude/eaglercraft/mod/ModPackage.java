package net.lax1dude.eaglercraft.mod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** A validated mod package ready for a platform-specific loader. */
public final class ModPackage {

    private final String id;
    private final String title;
    private final String description;
    private final String version;
    private final String source;
    private final String format;
    private final Map<String, byte[]> payloads;

    ModPackage(String id, String title, String description, String version, Map<String, byte[]> payloads) {
        this(id, title, description, version, null, null, payloads);
    }

    ModPackage(String id, String title, String description, String version, String source, String format,
            Map<String, byte[]> payloads) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.version = version;
        this.source = source;
        this.format = format;
        this.payloads = Collections.unmodifiableMap(new LinkedHashMap<>(payloads));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getSource() {
        return source;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, byte[]> getPayloads() {
        return payloads;
    }
}
