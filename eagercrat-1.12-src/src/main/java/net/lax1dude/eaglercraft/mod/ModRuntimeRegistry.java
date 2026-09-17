package net.lax1dude.eaglercraft.mod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Central runtime registry for mod lifecycle, config, and enabled state. */
public final class ModRuntimeRegistry {
    private final Map<String, ModPackage> packages = new LinkedHashMap<>();
    private final Map<String, Boolean> enabled = new LinkedHashMap<>();
    private final ModConfigStore configStore = new ModConfigStore();

    public synchronized void install(ModPackage modPackage) {
        packages.put(modPackage.getId(), modPackage);
        enabled.putIfAbsent(modPackage.getId(), Boolean.TRUE);
    }

    public synchronized boolean remove(String modId) {
        enabled.remove(modId);
        configStore.clear(modId);
        return packages.remove(modId) != null;
    }

    public synchronized ModPackage get(String modId) {
        return packages.get(modId);
    }

    public synchronized Map<String, ModPackage> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(packages));
    }

    public synchronized void setEnabled(String modId, boolean value) {
        if (!packages.containsKey(modId)) {
            throw new IllegalArgumentException("Unknown mod id: " + modId);
        }
        enabled.put(modId, Boolean.valueOf(value));
    }

    public synchronized boolean isEnabled(String modId) {
        return Boolean.TRUE.equals(enabled.get(modId));
    }

    public ModConfigStore getConfigStore() {
        return configStore;
    }
}
