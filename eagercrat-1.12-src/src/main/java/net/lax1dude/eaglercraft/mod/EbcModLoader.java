package net.lax1dude.eaglercraft.mod;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads the executable EBC payload from an imported package. */
public final class EbcModLoader implements ModLoader {
    private final EaglerModHost host;
    private final Map<String, ModPackage> loadedPackages = new LinkedHashMap<>();

    public EbcModLoader(EaglerModHost host) {
        this.host = host;
    }

    @Override
    public boolean supports(ModPackage modPackage) {
        for (String name : modPackage.getPayloads().keySet()) {
            if (name.endsWith(".ebc.fbm")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void load(ModPackage modPackage) throws IOException {
        boolean loaded = false;
        ModPackage previous = loadedPackages.put(modPackage.getId(), modPackage);
        if (previous != null && !previous.getId().equals(modPackage.getId())) {
            host.onUnload(previous.getId());
        }
        for (Map.Entry<String, byte[]> payload : modPackage.getPayloads().entrySet()) {
            if (payload.getKey().endsWith(".ebc.fbm")) {
                EbcInterpreter.execute(modPackage, payload.getValue(), host);
                loaded = true;
            }
        }
        if (!loaded) {
            loadedPackages.remove(modPackage.getId());
            throw new IOException("Package has no EBC payload");
        }
    }

    public void unload(String modId) {
        ModPackage previous = loadedPackages.remove(modId);
        if (previous != null) {
            host.onUnload(modId);
        }
    }
}
