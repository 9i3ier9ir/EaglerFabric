package net.lax1dude.eaglercraft.mod;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** In-memory package registry used by the mod menu and platform storage adapters. */
public final class ModPackageRegistry {

    private final Map<String, ModPackage> packages = new LinkedHashMap<>();

    public ModPackage install(InputStream input) throws IOException {
        ModPackage modPackage = ModPackageReader.read(input);
        synchronized (packages) {
            packages.put(modPackage.getId(), modPackage);
        }
        return modPackage;
    }

    public boolean remove(String id) {
        synchronized (packages) {
            return packages.remove(id) != null;
        }
    }

    public ModPackage get(String id) {
        synchronized (packages) {
            return packages.get(id);
        }
    }

    public Collection<ModPackage> getAll() {
        synchronized (packages) {
            return Collections.unmodifiableList(new java.util.ArrayList<>(packages.values()));
        }
    }

    public void clear() {
        synchronized (packages) {
            packages.clear();
        }
    }
}
