package net.lax1dude.eaglercraft.mod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Simple per-mod config store used by the custom Eagler mod runtime. */
public final class ModConfigStore {
    private final Map<String, Map<String, String>> values = new LinkedHashMap<>();

    public synchronized void register(String modId, String key, String defaultValue) {
        Map<String, String> map = values.computeIfAbsent(modId, ignored -> new LinkedHashMap<>());
        if (!map.containsKey(key)) {
            map.put(key, defaultValue);
        }
    }

    public synchronized void set(String modId, String key, String value) {
        Map<String, String> map = values.computeIfAbsent(modId, ignored -> new LinkedHashMap<>());
        map.put(key, value);
    }

    public synchronized String get(String modId, String key) {
        Map<String, String> map = values.get(modId);
        return map == null ? null : map.get(key);
    }

    public synchronized String get(String modId, String key, String defaultValue) {
        String value = get(modId, key);
        return value == null ? defaultValue : value;
    }

    public synchronized Map<String, String> snapshot(String modId) {
        Map<String, String> map = values.get(modId);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public synchronized void clear(String modId) {
        values.remove(modId);
    }
}
